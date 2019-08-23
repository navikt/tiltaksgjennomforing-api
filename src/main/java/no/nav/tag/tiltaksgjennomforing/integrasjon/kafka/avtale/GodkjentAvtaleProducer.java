package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.avtale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.Topics;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Component
@Slf4j
@RequiredArgsConstructor
public class GodkjentAvtaleProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void sendAvtaleTilJournalfoering(String avtaleId, String avtaleJson) {
        kafkaTemplate.send(Topics.AVTALE_GODKJENT, avtaleId, avtaleJson);
        log.info("Avtale med Id={} sendt på Kafka topic", avtaleId);
    }
}
