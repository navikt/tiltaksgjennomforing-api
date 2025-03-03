package no.nav.tag.tiltaksgjennomforing.arena.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakgjennomforingRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@RestController
@RequestMapping("/utvikler-admin/arena")
@Slf4j
@RequiredArgsConstructor
public class ArenaAdminController {
    private final ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository;
    private final EregService eregService;

    @GetMapping("/tiltak/{tiltakstype}/sjekk-ereg")
    public Map<String, ?> sjekkOppfolgingsstatus(
        @PathVariable String tiltakstype,
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "1000") Integer size
    ) {
        Pageable pageable = PageRequest.of(Math.abs(page), Math.abs(size));

        Map<BedriftNr, Optional<String>> enheter = tiltakgjennomforingRepository
            .findVirksomhetsnummerByTiltakskode(ArenaTiltakskode.parse(tiltakstype), pageable)
            .stream()
            .collect(Collectors.toMap(
                BedriftNr::new,
                bedriftNr -> {
                    try {
                        eregService.hentVirksomhet(new BedriftNr(bedriftNr));
                        return Optional.empty();
                    } catch (Exception e) {
                        if (e instanceof FeilkodeException) {
                            return Optional.of(((FeilkodeException) e).getFeilkode().name());
                        }
                        return Optional.of(e.getMessage());
                    }
                },
                (first, second) -> first.isPresent() ? first : second
            ));

        return Map.of(
            "totalt", enheter.size(),
            "gjennomfort", enheter.values().stream().filter(Optional::isEmpty).count(),
            "failet", enheter.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()))
        );
    }

}
