package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = { "tiltaksgjennomforing.kafka.enabled=true" })
@EmbeddedKafka(partitions = 1, topics = { Topics.SMS_VARSEL, VarselTopics.TILTAK_VARSEL })
@DirtiesContext
@ActiveProfiles({ Miljø.LOCAL })
class RefusjonKlarConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
     private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void skal_sende_sms_når_det_leses_varsel_kafkamelding() throws JSONException, JsonProcessingException {
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvBeslutter();
        avtaleRepository.save(avtale);

        var varselMelding = new RefusjonVarselMelding(avtale.getId().toString(), VarselType.KLAR);
        String meldingSomString = objectMapper.writeValueAsString(varselMelding);
        Header header = new RecordHeader("__TypeId__", "no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonVarselMelding".getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(new ProducerRecord<String, String>(VarselTopics.TILTAK_VARSEL, null, "123", meldingSomString, List.of(header)));

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var consumer = consumerFactory.createConsumer();

        embeddedKafka.consumeFromAllEmbeddedTopics(consumer);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.SMS_VARSEL);
        JSONObject jsonRefusjonRecord = new JSONObject(record.value());

        String meldingstekst = "Dere kan nå søke om refusjon for tilskudd til sommerjobb. Frist for å søke er to måneder etter tiltaket er avsluttet. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.";
        assertThat(jsonRefusjonRecord.get("meldingstekst")).isEqualTo(meldingstekst);
        assertThat(jsonRefusjonRecord.get("telefonnummer")).isEqualTo(avtale.getArbeidsgiverTlf());
        assertThat(jsonRefusjonRecord.get("identifikator")).isEqualTo(avtale.getBedriftNr().asString());

    }

}