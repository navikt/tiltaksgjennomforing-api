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
import java.util.ArrayList;
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
            AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, LocalDateTime.now(), meldingId, DvhHendelseType.PATCHING);
            dvhMeldingRepository.save(new DvhMeldingEntitet(meldingId, avtale.getId(), LocalDateTime.now(), avtale.statusSomEnum(), avroTiltakHendelse));
            log.info("Patchet avtale {}, sendt melding med id {} til datavarehus", avtale.getId(), meldingId);
        });
    }

//    @GetMapping("/migrer")
//    public void migrer() {
//        if (!tokenUtils.harAdGruppe(poArbeidsgiverAadProperties.getId())) {
//            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
//        }
//        avtaleRepository.findAll().forEach(avtale -> {
//            if (!dvhMeldingFilter.skalTilDatavarehus(avtale)) {
//                return;
//            }
//            if (!dvhMeldingRepository.existsByAvtaleId(avtale.getId())) {
//                LocalDateTime now = LocalDateTime.now();
//                AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, now, UUID.randomUUID(), DvhHendelseType.MIGRERING);
//                dvhMeldingRepository.save(new DvhMeldingEntitet(UUID.randomUUID(), avtale.getId(), now, avtale.statusSomEnum(), avroTiltakHendelse));
//            }
//        });
//    }

    @PostMapping("/migrer-dry-run")
    public List<DvhMeldingEntitet> migrerDryRun(@RequestBody MigrerRequest request) {
        if (!tokenUtils.harAdGruppe(dvhMeldingProperties.getGruppeTilgang())) {
            throw new RuntimeException("Ikke tilgang");
        }
        var liste = new ArrayList<DvhMeldingEntitet>();
        avtaleRepository.findAll().forEach(avtale -> {
            if (avtale.erGodkjentAvVeileder() && request.tiltakstype == avtale.getTiltakstype()) {
                if (!dvhMeldingRepository.existsByAvtaleId(avtale.getId())) {
                    LocalDateTime now = LocalDateTime.now();
                    AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, now, UUID.randomUUID(), DvhHendelseType.MIGRERING);
                    liste.add(new DvhMeldingEntitet(UUID.randomUUID(), avtale.getId(), now, avtale.statusSomEnum(), avroTiltakHendelse));
                }
            }
        });
        return liste;
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
