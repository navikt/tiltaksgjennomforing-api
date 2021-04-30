package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/dvh-melding")
@RequiredArgsConstructor
@Unprotected
@ProtectedWithClaims(issuer = "isso")
public class InternalDvhMeldingProdusentController {
    private final AvtaleRepository avtaleRepository;
    private final DvhMeldingEntitetRepository dvhMeldingRepository;
    private final TokenUtils tokenUtils;
    private final DvhMeldingProperties dvhMeldingProperties;
    private final DvhMeldingFilter dvhMeldingFilter;

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
    private static class MigrerRequest {
        Tiltakstype tiltakstype;
    }
}
