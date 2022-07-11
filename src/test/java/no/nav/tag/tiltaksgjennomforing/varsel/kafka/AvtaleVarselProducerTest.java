package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = { "tiltaksgjennomforing.kafka.enabled=true" })
@DirtiesContext
@ActiveProfiles({ Miljø.LOCAL })
@EnableKafka
@EmbeddedKafka(partitions = 1, topics = { Topics.AVTALE_VARSEL })
public class AvtaleVarselProducerTest {

    @Autowired
    private AvtaleVarselProducer avtaleVarselProducer;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    public void Test_avtale_varsel_producer() throws InterruptedException {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        Avtalerolle utfortAv = Avtalerolle.VEILEDER;
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setArbeidsgiverTlf("45342334");
        AvtaleEndret avtaleEndret = new AvtaleEndret(avtale, utfortAv);

        avtaleVarselProducer.publiserMelding(AvtalePubliseringsType.ENDRET, avtaleEndret.getAvtale());
        Thread.sleep(1000L);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", this.embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer()
        );
        Consumer<String, RefusjonVarselMelding> consumer = consumerFactory.createConsumer();
        this.embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, Topics.AVTALE_VARSEL);
        ConsumerRecords<String, RefusjonVarselMelding> replies = KafkaTestUtils.getRecords(consumer);
        assertThat(replies.count()).isGreaterThanOrEqualTo(1);
        Now.resetClock();
    }

}
