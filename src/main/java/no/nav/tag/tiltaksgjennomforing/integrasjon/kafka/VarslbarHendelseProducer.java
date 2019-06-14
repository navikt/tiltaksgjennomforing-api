package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RequiredArgsConstructor
public class VarslbarHendelseProducer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static String serialiserMelding(VarslbarHendelse varslbarHendelse) {
        try {
            return OBJECT_MAPPER.writeValueAsString(varslbarHendelse);
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere varslbar hendelse", e);
            throw new TiltaksgjennomforingException("Kunne ikke serialisere varslbar hendelse");
        }
    }

    public void sendVarslbarHendelse(VarslbarHendelse varslbarHendelse) {
        try {
            kafkaTemplate.send(
                    Topics.VARSLBAR_HENDELSE_OPPSTAATT,
                    serialiserMelding(varslbarHendelse)).get();
            log.info("Avtale med id {} sendt på Kafka topic", varslbarHendelse.getAvtaleId());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kunne ikke sende avtale på Kafka topic", e);
            throw new TiltaksgjennomforingException("Kunne ikke sende avtale på Kafka topic");
        }
    }
}
