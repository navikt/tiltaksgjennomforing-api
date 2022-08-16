package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
public class RefusjonEndretBetalingsstatusKafkaConsumer {
    private final TilskuddPeriodeRepository tilskuddPeriodeRepository;
    private final ObjectMapper objectMapper;

    public RefusjonEndretBetalingsstatusKafkaConsumer(TilskuddPeriodeRepository tilskuddPeriodeRepository, ObjectMapper objectMapper) {
        this.tilskuddPeriodeRepository = tilskuddPeriodeRepository;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = Topics.REFUSJON_ENDRET_BETALINGSSTATUS, containerFactory = "refusjonContainerFactory")
    public void refusjonEndretBetalingsstatus(String jsonMelding) throws JsonProcessingException {
        RefusjonEndretBetalingsstatusMelding melding = objectMapper.readValue(jsonMelding, RefusjonEndretBetalingsstatusMelding.class);

        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(UUID.fromString(melding.getTilskuddsperiodeId())).orElseThrow();

        if(melding.getStatus() == TilskuddsperiodeUtbetaltStatus.UTBETALT) {
            tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UTBETALT);
        }
        tilskuddPeriodeRepository.save(tilskuddPeriode);
    }
}
