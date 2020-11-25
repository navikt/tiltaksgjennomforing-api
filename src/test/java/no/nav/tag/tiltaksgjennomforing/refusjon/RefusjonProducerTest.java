package no.nav.tag.tiltaksgjennomforing.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"tiltaksgjennomforing.kafka.enabled=true"})
@DirtiesContext
@ActiveProfiles({Miljø.LOCAL})
@EmbeddedKafka(partitions = 1, controlledShutdown = false, topics = {Topics.REFUSJON})
class RefusjonProducerTest {

  @Autowired
  private RefusjonProducer refusjonProducer;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafka;

  private Consumer<String, String> consumer;

  @BeforeEach
  public void setUp() {

    Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
    consumer = consumerFactory.createConsumer();
    embeddedKafka.consumeFromAnEmbeddedTopic(consumer, Topics.REFUSJON);
  }

  @Test
  public void refusjonProducer__skal_sendes_på_kafka_topic_med_riktige_felter() throws JSONException, InterruptedException {

    // GITT
    final String melding = "Hei";

    //NÅR
    refusjonProducer.publiserRefusjonsmelding(melding);

    //SÅ
    ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.REFUSJON);
    String v = record.value();
    assertThat(v).isEqualTo(melding);
  }
}