package no.nav.tag.tiltaksgjennomforing.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
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
  public void skal_kunne_sende_refusjonsmelding_på_kafka_topic() throws JSONException {

    // GITT
    Refusjonsmelding refusjonsmelding = new Refusjonsmelding();
    final String deltakerFnr = "09876543211";
    refusjonsmelding.setDeltakerFnr(deltakerFnr);

    //NÅR
    refusjonProducer.publiserRefusjonsmelding(refusjonsmelding);

    //SÅ
    ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.REFUSJON);
    JSONObject jsonRefusjonRecord = new JSONObject(record.value());
    assertThat(jsonRefusjonRecord.get("tilskuddPeriodeId")).isNotNull();
    assertThat(jsonRefusjonRecord.get("avtaleInnholdId")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tiltakstype")).isNotNull();
    assertThat(jsonRefusjonRecord.get("deltakerFnr")).isNotNull();
    assertThat(jsonRefusjonRecord.get("veilederNavIdent")).isNotNull();
    assertThat(jsonRefusjonRecord.get("bedriftnummer")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tilskuddBeløp")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tilskuddFraDato")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tilskuddTilDato")).isNotNull();
    assertThat(jsonRefusjonRecord.get("opprettetTidspunkt")).isNotNull();
    assertThat(jsonRefusjonRecord.get("deltakerFnr")).isEqualTo(deltakerFnr);
  }
}