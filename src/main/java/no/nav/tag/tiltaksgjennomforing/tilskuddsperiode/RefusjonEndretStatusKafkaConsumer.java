package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    public RefusjonEndretStatusKafkaConsumer(
        TilskuddPeriodeRepository tilskuddPeriodeRepository,
        ObjectMapper objectMapper
    ) {
        this.tilskuddPeriodeRepository = tilskuddPeriodeRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.REFUSJON_ENDRET_STATUS)
    public void refusjonEndretStatus(String melding) throws JsonProcessingException {
        RefusjonEndretStatusMelding refusjonEndretStatusMelding = objectMapper.readValue(melding, RefusjonEndretStatusMelding.class);
        log.info("Mottok melding om endret avtale {} refusjonsstatus for tilskuddsperiode {} med status {}", refusjonEndretStatusMelding.getAvtaleId(), refusjonEndretStatusMelding.getTilskuddsperiodeId(), refusjonEndretStatusMelding.getStatus());
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(UUID.fromString(refusjonEndretStatusMelding.getTilskuddsperiodeId())).orElseThrow();
        if(tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
            log.error("En tilskuddsperiode {} som er ubehandlet har fått statusendring fra refusjon-api", refusjonEndretStatusMelding.getTilskuddsperiodeId());
        }
        tilskuddPeriode.setRefusjonStatus(refusjonEndretStatusMelding.getStatus());
        log.info("Setter refusjonstatus til {} på tilskuddsperiode {}", refusjonEndretStatusMelding.getStatus(), refusjonEndretStatusMelding.getTilskuddsperiodeId());

        tilskuddPeriodeRepository.save(tilskuddPeriode);

    }
}
