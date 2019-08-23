package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.avtale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatus;
import no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatusReopsitory;
import no.nav.tag.tiltaksgjennomforing.domene.prosess.StatusJournalforing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatus.FEILET_IKKE_SENDT;
import static no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatus.TIL_PROSESS;

@Service
@Slf4j

public class JournalfoeringService {

    @Autowired
    private JournalforingStatusReopsitory journalForingStatusReopsitory;

    private GodkjentAvtaleProducer godkjentAvtaleProducer;

    private static ObjectMapper avtaleObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void sendTilJournalfoeringHvisGodkjentAvAlle(Avtale avtale) {
        if (!avtale.erGodkjentAvVeileder()) {
            return;
        }

        try {
            godkjentAvtaleProducer.sendAvtaleTilJournalfoering(avtale.getId().toString(), avtaleObjectMapper.writeValueAsString(avtale));
            lagreStatus(avtale.getId(), TIL_PROSESS);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Feil ved sending av avtale på Kafka. AvtaleId={}", avtale.getId(), e.getMessage());
            lagreStatus(avtale.getId(), FEILET_IKKE_SENDT);
        }
    }

    private void lagreStatus(UUID avtaleId, JournalforingStatus status) {
        StatusJournalforing statusJournalforing = new StatusJournalforing(avtaleId, status);
        journalForingStatusReopsitory.save(statusJournalforing);
    }
}


