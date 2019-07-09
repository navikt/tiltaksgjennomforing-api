package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.Topics;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.stereotype.Component;

@Profile("kafka-test")
@Slf4j
@Component
public class KafkaMockServer implements DisposableBean {
    private final EmbeddedKafkaBroker embeddedKafka;

    public KafkaMockServer() {
        log.info("Starter embedded Kafka");
        embeddedKafka = new EmbeddedKafkaBroker(3, false, Topics.alleTopics());
        embeddedKafka.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        log.info("Stopper embedded Kafka");
        embeddedKafka.destroy();
    }

    public EmbeddedKafkaBroker getEmbeddedKafka() {
        return embeddedKafka;
    }
}
