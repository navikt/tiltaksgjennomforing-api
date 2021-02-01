package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
@EmbeddedKafka(partitions = 1, topics = {Topics.REFUSJON})
class TilskuddsperiodeKafkaProducerTest {

  @Autowired
  private TilskuddsperiodeKafkaProducer tilskuddsperiodeKafkaProducer;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafka;

  @MockBean
  private FeatureToggleService featureToggleService;

  private Consumer<String, String> consumer;

  @BeforeEach
  public void setUp() {
    when(featureToggleService.isEnabled(anyString())).thenReturn(true);

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
    final UUID avtaleId = UUID.randomUUID();
    final UUID tilskuddPeriodeId = UUID.randomUUID();
    final UUID avtaleInnholdId = UUID.randomUUID();
    final Tiltakstype tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD;
    final String deltakerFornavn = "Donald";
    final String deltakerEtternavn = "Duck";
    final Identifikator deltakerFnr = new Fnr("12345678901");
    final NavIdent veilederNavIdent = new NavIdent("X123456");
    final String bedriftNavn = "Donald Delivery";
    final BedriftNr bedriftnummer = new BedriftNr("99999999");
    final Integer tilskuddBeløp = 12000;
    final LocalDate tilskuddFraDato = LocalDate.now().minusDays(15);
    final LocalDate tilskuddTilDato = LocalDate.now().plusMonths(2);

    final TilskuddsperiodeGodkjentMelding tilskuddMelding = new TilskuddsperiodeGodkjentMelding(avtaleId,
        tilskuddPeriodeId, avtaleInnholdId, tiltakstype, deltakerFornavn, deltakerEtternavn,
        deltakerFnr, veilederNavIdent, bedriftNavn, bedriftnummer, tilskuddBeløp, tilskuddFraDato, tilskuddTilDato, 10.6, 0.02, 14.1, 60);

    //NÅR
    tilskuddsperiodeKafkaProducer.publiserTilskuddsperiodeGodkjentMelding(tilskuddMelding);

    //SÅ
    ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.REFUSJON);
    JSONObject jsonRefusjonRecord = new JSONObject(record.value());
    assertThat(jsonRefusjonRecord.get("avtaleId")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tilskuddsperiodeId")).isNotNull();
    assertThat(jsonRefusjonRecord.get("avtaleInnholdId")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tiltakstype")).isNotNull();
    assertThat(jsonRefusjonRecord.get("deltakerFornavn")).isNotNull();
    assertThat(jsonRefusjonRecord.get("deltakerEtternavn")).isNotNull();
    assertThat(jsonRefusjonRecord.get("deltakerFnr")).isNotNull();
    assertThat(jsonRefusjonRecord.get("veilederNavIdent")).isNotNull();
    assertThat(jsonRefusjonRecord.get("bedriftNavn")).isNotNull();
    assertThat(jsonRefusjonRecord.get("bedriftNr")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tilskuddsbeløp")).isNotNull();
    assertThat(jsonRefusjonRecord.get("tilskuddFom")).isNotNull().isOfAnyClassIn(String.class);
    assertThat(jsonRefusjonRecord.get("tilskuddTom")).isNotNull().isOfAnyClassIn(String.class);
    assertThat(jsonRefusjonRecord.get("feriepengerSats")).isNotNull();
    assertThat(jsonRefusjonRecord.get("otpSats")).isNotNull();
    assertThat(jsonRefusjonRecord.get("arbeidsgiveravgiftSats")).isNotNull();
    assertThat(jsonRefusjonRecord.get("lønnstilskuddsprosent")).isNotNull();
  }
}