package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.Varsel;
import no.nav.tag.tiltaksgjennomforing.domene.VarselService;
import no.nav.tag.tiltaksgjennomforing.domene.VarslbarHendelse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class VarslbarHendelseConsumer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final VarselService varselService;

    @KafkaListener(topics = Topics.VARSLBAR_HENDELSE_OPPSTAATT)
    public void consume(String message) {
        log.info("Consumed message -> {}", message);
        try {
            VarslbarHendelse varslbarHendelse = OBJECT_MAPPER.readValue(message, VarslbarHendelse.class);
            for (Varsel varsel : varslbarHendelse.getVarsler()) {
                varselService.sendVarsel(varsel);
            }
        } catch (IOException e) {
            log.error("Feil ved mapping av JSON", e);
        }
    }
}
