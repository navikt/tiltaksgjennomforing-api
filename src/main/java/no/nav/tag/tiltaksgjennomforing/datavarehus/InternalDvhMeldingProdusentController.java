package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/utvikler-admin/dvh-melding")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@Slf4j
public class InternalDvhMeldingProdusentController {
    private final AvtaleRepository avtaleRepository;
    private final DvhMeldingEntitetRepository dvhMeldingRepository;
    private final TokenUtils tokenUtils;
    private final DvhAvtalePatchService dvhAvtalePatchService;

    @PostMapping("/patch")
    public void patcheAvtale(@RequestBody PatchRequest request) {
        log.info("Patcher avtaler til dvh");
        avtaleRepository.findAllById(request.avtaleIder()).forEach(avtale -> {
            UUID meldingId = UUID.randomUUID();
            String utførtAv = tokenUtils.hentBrukerOgIssuer().map(TokenUtils.BrukerOgIssuer::getBrukerIdent).orElse("patch");
            AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, DvhHendelseType.PATCHING, utførtAv);
            dvhMeldingRepository.save(new DvhMeldingEntitet(avtale, avroTiltakHendelse));
            log.info("Patchet avtale {}, sendt melding med id {} til datavarehus", avtale.getId(), meldingId);
        });
    }

    @PostMapping("patchalleavtaler")
    public void patchAlleAvtaler() {
        log.info("Patcher alle avtaler til dvh");
        dvhAvtalePatchService.lagDvhPatchMeldingForAlleAvtaler();
    }

    @PostMapping("patchtiltakstype/{tiltakstype}")
    public void patchTiltakstype(@PathVariable Tiltakstype tiltakstype) {
        log.info("Patcher avtaler med tiltakstype {} til dvh", tiltakstype);
        dvhAvtalePatchService.lagDvhPatchMeldingerForTiltakstype(tiltakstype);
    }

    private record PatchRequest(List<UUID> avtaleIder) {
    }

}
