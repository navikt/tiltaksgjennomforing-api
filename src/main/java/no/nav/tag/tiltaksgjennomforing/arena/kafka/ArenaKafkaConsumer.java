package no.nav.tag.tiltaksgjennomforing.arena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.configuration.ArenaKafkaProperties;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventProcessingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.LOCAL, Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaKafkaConsumer {
    private final ArenaKafkaProperties arenaKafkaProperties;
    private final ArenaEventProcessingService arenaEventProcessingService;
    private final FeatureToggleService featureToggleService;

    public ArenaKafkaConsumer(
        ArenaEventProcessingService arenaEventProcessingService,
        ArenaKafkaProperties arenaKafkaProperties,
        FeatureToggleService featureToggleService
    ) {
        this.arenaEventProcessingService = arenaEventProcessingService;
        this.arenaKafkaProperties = arenaKafkaProperties;
        this.featureToggleService = featureToggleService;
    }

    @KafkaListener(topics = "${tiltaksgjennomforing.arena.kafka.tiltakgjennomforing-endret-topic}", containerFactory = "arenaContainerFactory")
    public void arenaTiltakgjennomforingEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for {}: {}", arenaKafkaProperties.getTiltakgjennomforingEndretTopic(), record.key());
        arenaEventProcessingService.create(record.key(), record.value());
    }

    @KafkaListener(topics = "${tiltaksgjennomforing.arena.kafka.tiltakdeltaker-endret-topic}", containerFactory = "arenaContainerFactory")
    public void arenaTiltakdeltakerEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for {}: {}", arenaKafkaProperties.getTiltakdeltakerEndretTopic(), record.key());
        arenaEventProcessingService.create(record.key(), record.value());
    }
}
