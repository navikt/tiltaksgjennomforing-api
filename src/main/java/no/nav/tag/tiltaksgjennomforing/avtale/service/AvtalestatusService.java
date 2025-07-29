package no.nav.tag.tiltaksgjennomforing.avtale.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMelding;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitet;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.datavarehus.AvroTiltakHendelse;
import no.nav.tag.tiltaksgjennomforing.datavarehus.AvroTiltakHendelseFabrikk;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhHendelseType;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhMeldingEntitet;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AvtalestatusService {
    private final ObjectMapper objectMapper;
    private final AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;
    private final AvtaleRepository avtaleRepository;
    private final DvhMeldingEntitetRepository dvhMeldingRepository;

    public AvtalestatusService(
        AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository,
        AvtaleRepository avtaleRepository,
        DvhMeldingEntitetRepository dvhMeldingRepository,
        ObjectMapper objectMapper
    ) {
        this.avtaleMeldingEntitetRepository = avtaleMeldingEntitetRepository;
        this.avtaleRepository = avtaleRepository;
        this.dvhMeldingRepository = dvhMeldingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void oppdaterAvtalerSomKreverEndringAvStatus() {
        avtaleRepository.findAvtalerForEndringAvStatus().forEach(avtale -> {
            Status status = Status.fra(avtale);
            if (avtale.getStatus().equals(status)) {
                return;
            }

            log.info(
                "Avtale med id {} har endret status fra {} til {}. Avtalen blir oppdatert.",
                avtale.getId(),
                avtale.getStatus(),
                status
            );

            avtale.setStatus(status);
            avtaleRepository.save(avtale);

            sendAvtaleMelding(avtale);
            sendMeldingTilDvh(avtale);
        });
    }

    private void sendMeldingTilDvh(Avtale avtale) {
        AvroTiltakHendelse avroTiltakHendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, DvhHendelseType.STATUSENDRING, "system");
        dvhMeldingRepository.save(new DvhMeldingEntitet(avtale, avroTiltakHendelse));
    }

    private void sendAvtaleMelding(Avtale avtale) {
        LocalDateTime tidspunkt = Now.localDateTime();
        AvtaleMelding avtaleMelding = AvtaleMelding.create(avtale, avtale.getGjeldendeInnhold(), new Identifikator("tiltaksgjennomforing-api"), AvtaleHendelseUtførtAvRolle.SYSTEM, HendelseType.STATUSENDRING);
        try {
            String meldingSomString = objectMapper.writeValueAsString(avtaleMelding);
            AvtaleMeldingEntitet entitet = new AvtaleMeldingEntitet(UUID.randomUUID(), avtale.getId(), tidspunkt, HendelseType.STATUSENDRING, avtale.getStatus(), meldingSomString);
            avtaleMeldingEntitetRepository.save(entitet);
        } catch (JsonProcessingException e) {
            log.error("Feil ved parsing av AvtaleHendelseMelding i statusendringjobb til json for hendelse med avtaleId {}", avtaleMelding.getAvtaleId());
            throw new RuntimeException(e);
        }
    }

}
