package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/dvh-melding")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "isso")
@Slf4j
public class InternalDvhMeldingProdusentController {
    private final AvtaleRepository avtaleRepository;
    private final DvhMeldingEntitetRepository dvhMeldingRepository;
    private final TokenUtils tokenUtils;
    private final DvhMeldingProperties dvhMeldingProperties;
    private final DvhMeldingFilter dvhMeldingFilter;

    @PostMapping("/patch")
    public void patcheAvtale(@RequestBody PatchRequest request) {
        if (!tokenUtils.harAdGruppe(dvhMeldingProperties.getGruppeTilgang())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
        avtaleRepository.findAllById(request.getAvtaleIder()).forEach(avtale -> {
            UUID meldingId = UUID.randomUUID();
            String utførtAv = tokenUtils.hentBrukerOgIssuer().map(TokenUtils.BrukerOgIssuer::getBrukerIdent).orElse("patch");
            AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, LocalDateTime.now(), meldingId, DvhHendelseType.PATCHING, utførtAv);
            dvhMeldingRepository.save(new DvhMeldingEntitet(meldingId, avtale.getId(), LocalDateTime.now(), avtale.statusSomEnum(), avroTiltakHendelse));
            log.info("Patchet avtale {}, sendt melding med id {} til datavarehus", avtale.getId(), meldingId);
        });
    }

    @Value
    private static class PatchRequest {
        List<UUID> avtaleIder;
    }

    @Value
    private static class MigrerRequest {
        Tiltakstype tiltakstype;
    }
}
