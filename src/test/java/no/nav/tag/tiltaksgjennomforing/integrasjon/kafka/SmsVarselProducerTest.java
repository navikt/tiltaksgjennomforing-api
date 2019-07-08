package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelseRepository;
import no.nav.tag.tiltaksgjennomforing.integrasjon.KafkaMockServer;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles({"dev", "kafka", "kafka-test"})
public class SmsVarselProducerTest {

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private KafkaMockServer embeddedKafka;
    @Autowired
    private VarslbarHendelseRepository repository;

    private Consumer<String, String> consumer;

    @Before
    public void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        embeddedKafka.getEmbeddedKafka().consumeFromAnEmbeddedTopic(consumer, Topics.SMS_VARSEL);
    }

    @Test
    public void varslbarHendelseOppstaatt__skal_sendes_på_kafka_topic_med_riktige_felter() throws JSONException {
        VarslbarHendelse varslbarHendelse = TestData.enHendelseMedSmsVarsel(TestData.enAvtaleMedAltUtfylt());
        transactionTemplate.execute(status -> {
            repository.save(varslbarHendelse);
            return status;
        });

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.SMS_VARSEL);
        JSONObject json = new JSONObject(record.value());
        assertThat(json.getString("smsVarselId"))
                .isNotNull()
                .isIn(varslbarHendelse.getSmsVarsler().stream().map(smsVarsel -> smsVarsel.getId().toString()).collect(Collectors.toList()));
    }
}
