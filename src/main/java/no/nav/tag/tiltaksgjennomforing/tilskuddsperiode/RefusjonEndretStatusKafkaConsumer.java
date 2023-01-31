package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class RefusjonEndretStatusKafkaConsumer {
    private final TilskuddPeriodeRepository tilskuddPeriodeRepository;
    private final ObjectMapper objectMapper;


    public RefusjonEndretStatusKafkaConsumer(TilskuddPeriodeRepository tilskuddPeriodeRepository, ObjectMapper objectMapper) {
        this.tilskuddPeriodeRepository = tilskuddPeriodeRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.REFUSJON_ENDRET_STATUS, containerFactory = "refusjonEndretStatusContainerFactory")
    public void refusjonEndretStatus(String jsonMelding) throws JsonProcessingException {
        RefusjonEndretStatusMelding melding = objectMapper.readValue(jsonMelding, RefusjonEndretStatusMelding.class);

        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(UUID.fromString(melding.getTilskuddsperiodeId())).orElseThrow();
        if(tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.GODKJENT) {
            log.error("En tilskuddsperiode {} som ikke er godkjent av beslutter har fått statusendring fra refusjon-api", melding.getTilskuddsperiodeId());
        }
        tilskuddPeriode.setRefusjonStatus(melding.getStatus());
        log.info("Setter refusjonstatus til {} på tilskuddsperiode {}", melding.getStatus(), melding.getTilskuddsperiodeId());

        tilskuddPeriodeRepository.save(tilskuddPeriode);

    }
}
