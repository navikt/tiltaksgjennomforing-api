package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelseRepository;
import no.nav.tag.tiltaksgjennomforing.integrasjon.KafkaMockServer;
import no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.avtale.GodkjentAvtaleProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles({"dev", "kafka", "kafka-test"})
public class KafkaProducerTest {

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private KafkaMockServer embeddedKafka;
    @Autowired
    private VarslbarHendelseRepository repository;
    @Autowired
    GodkjentAvtaleProducer godkjentAvtaleProducer;

    private Consumer<String, String> consumer;

    @Before
    public void setUp(){
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
    }

    @Test
    public void sender_på_kafka_topic_med_riktige_felter() throws Exception {
        varslbarHendelseOppstaatt__skal_sendes_på_kafka_topic_med_riktige_felter();
        avtaleTilJournalfoering__skal_sendes_på_kafka_topic_med_riktige_felter();
    }


    private void varslbarHendelseOppstaatt__skal_sendes_på_kafka_topic_med_riktige_felter() throws JSONException {
        embeddedKafka.getEmbeddedKafka().consumeFromAnEmbeddedTopic(consumer, Topics.SMS_VARSEL);
        VarslbarHendelse varslbarHendelse = TestData.enHendelseMedSmsVarsel(TestData.enAvtaleMedAltUtfylt());
        transactionTemplate.execute(status -> repository.save(varslbarHendelse));

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.SMS_VARSEL);
        JSONObject json = new JSONObject(record.value());
        assertThat(json.getString("smsVarselId"))
                .isNotNull()
                .isIn(varslbarHendelse.getSmsVarsler().stream().map(smsVarsel -> smsVarsel.getId().toString()).collect(Collectors.toList()));
    }


    private void avtaleTilJournalfoering__skal_sendes_på_kafka_topic_med_riktige_felter() throws Exception {
        embeddedKafka.getEmbeddedKafka().consumeFromAnEmbeddedTopic(consumer, Topics.AVTALE_GODKJENT);

        final UUID AVTALE_ID = UUID.randomUUID();
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setId(AVTALE_ID);
        String avtaleJson = TestData.avtaleTilJson(avtale);

        godkjentAvtaleProducer.sendAvtaleTilJournalfoering(avtale.getId().toString(), avtaleJson);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.AVTALE_GODKJENT);
        assertThat(record.key()).isEqualTo(AVTALE_ID.toString());
        assertThat(record.value()).contains(AVTALE_ID.toString());
    }
}
