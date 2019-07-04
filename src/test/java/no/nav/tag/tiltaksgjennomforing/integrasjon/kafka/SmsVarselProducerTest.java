package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.events.VarslbarHendelseOppstaatt;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles({"dev", "kafka", "kafka-test"})
public class SmsVarselProducerTest {

    @Autowired
    public KafkaMockServer embeddedKafka;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
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
        Avtale avtale = TestData.enAvtale();
        avtale.settIdOgOpprettetTidspunkt();
        VarslbarHendelse varslbarHendelse = TestData.enHendelseMedSmsVarsel(avtale);
        varslbarHendelse.settIdOgOpprettetTidspunkt();
        eventPublisher.publishEvent(new VarslbarHendelseOppstaatt(avtale, varslbarHendelse));

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, Topics.SMS_VARSEL);

        JSONObject json = new JSONObject(record.value());
        assertThat(json.getString("smsVarselId"))
                .isNotNull()
                .isEqualTo(varslbarHendelse.getSmsVarsler().get(0).getId().toString());
    }
}
