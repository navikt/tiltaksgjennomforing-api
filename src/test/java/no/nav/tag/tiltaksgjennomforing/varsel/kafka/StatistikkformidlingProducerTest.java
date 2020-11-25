package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
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
@EmbeddedKafka(partitions = 1, controlledShutdown = false, topics = {Topics.STATISTIKKFORMIDLING})
class StatistikkformidlingProducerTest {

  @Autowired
  private StatistikkformidlingProducer statistikkFormidlingProducer;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafka;


  private Consumer<String, String> consumer;

  @BeforeEach
  public void setUp() {
    //when(featureToggleService.isEnabled(anyString())).thenReturn(true);

    Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
    consumer = consumerFactory.createConsumer();
    embeddedKafka.consumeFromAnEmbeddedTopic(consumer, Topics.STATISTIKKFORMIDLING);
  }

  @Test
  public void statisktikkformidlingMelding__skal_sendes_på_kafka_topic_med_riktige_felter() throws JSONException, InterruptedException {

    // GITT
    Fnr deltakerFnr = new Fnr("01234567890");
    NavIdent veilederNavIdent = new NavIdent("X123456");
    BedriftNr bedriftNr = new BedriftNr("000111222");
    Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.VARIG_LONNSTILSKUDD), veilederNavIdent);
    avtale.setLonnstilskuddProsent(60);
    avtale.setStillingstype(Stillingstype.FAST);
    avtale.setStillingstittel("utvikler");
    avtale.setDeltakerFornavn("Donald");
    avtale.setDeltakerEtternavn("Duck");

    //NÅR
    statistikkFormidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(avtale));

    //SÅ
    ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.STATISTIKKFORMIDLING);
    JSONObject json = new JSONObject(record.value());
    assertThat(json.getString("avtaleId")).isNotNull();
    assertThat(json.getString("organisasjonsnummer")).isEqualTo(bedriftNr.toString());
    assertThat(json.getString("stillingstype")).isEqualTo(Stillingstype.FAST.toString());
    assertThat(json.getString("tiltakstype")).isEqualTo(Tiltakstype.VARIG_LONNSTILSKUDD.toString());
    assertThat(json.getString("andelLonnstilskudd")).isEqualTo("60");
    assertThat(json.getString("yrke")).isEqualTo("utvikler");
    assertThat(json.getString("navn")).isEqualTo("Donald Duck");
  }
}