package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class DvhStatusendringJobb {
    private final DvhMeldingEntitetRepository dvhMeldingRepository;
    private final AvtaleRepository avtaleRepository;

    @Scheduled(fixedDelay = 10000)
    public void sjekkOmStatusendring() {
        dvhMeldingRepository.findNyesteDvhMeldingForAvtaleSomKanEndreStatus().forEach(dvhMeldingEntitet -> {
            UUID avtaleId = dvhMeldingEntitet.getAvtaleId();
            Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow();
            if (!DvhMeldingFilter.skalTilDatavarehus(avtale)) {
                return;
            }
            if (avtale.statusSomEnum() != dvhMeldingEntitet.getTiltakStatus()) {
                LocalDateTime tidspunkt = LocalDateTime.now();
                AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, tidspunkt, UUID.randomUUID(), DvhHendelseType.STATUSENDRING);
                dvhMeldingRepository.save(new DvhMeldingEntitet(UUID.randomUUID(), avtaleId, tidspunkt, avtale.statusSomEnum(), avroTiltakHendelse));
            }
        });
    }

//    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
//    public void sjekkOmStatusendringMetode2() {
//        avtaleRepository.findAll().forEach(avtale -> {
//            if (!DvhMeldingFilter.skalTilDatavarehus(avtale)) {
//                return;
//            }
//            if (avtale.erGodkjentAvVeileder() && avtale.getTiltakstype() == Tiltakstype.SOMMERJOBB && !dvhMeldingRepository.existsByAvtaleIdAndTiltakStatus(avtale.getId(), avtale.statusSomEnum())) {
//                AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, LocalDateTime.now(), UUID.randomUUID(), DvhHendelseType.STATUSENDRING);
//                dvhMeldingRepository.save(new DvhMeldingEntitet(UUID.randomUUID(), avtale.getId(), LocalDateTime.now(), avtale.statusSomEnum(), avroTiltakHendelse));
//            }
//        });
//    }

    @Scheduled(fixedDelay = 10000)
    public void lagMeldingerForEksisterendeAvtaler() {
        avtaleRepository.findAll().forEach(avtale -> {
            if (!DvhMeldingFilter.skalTilDatavarehus(avtale)) {
                return;
            }
            if (!dvhMeldingRepository.existsByAvtaleId(avtale.getId())) {
                LocalDateTime now = LocalDateTime.now();
                AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, now, UUID.randomUUID(), DvhHendelseType.MIGRERING);
                dvhMeldingRepository.save(new DvhMeldingEntitet(UUID.randomUUID(), avtale.getId(), now, avtale.statusSomEnum(), avroTiltakHendelse));
            }
        });
    }
}
