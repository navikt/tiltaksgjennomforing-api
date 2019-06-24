package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.VarselService;
import no.nav.tag.tiltaksgjennomforing.domene.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
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
        VarslbarHendelse varslbarHendelse;
        try {
            varslbarHendelse = OBJECT_MAPPER.readValue(message, VarslbarHendelse.class);
        } catch (IOException e) {
            log.error("Feil ved mapping av JSON", e);
            throw new TiltaksgjennomforingException("Feil ved mapping av JSON");
        }

        sendVarsler(varslbarHendelse);
    }

    private void sendVarsler(VarslbarHendelse varslbarHendelse) {
        int totalt = varslbarHendelse.getVarsler().size();
        for (int i = 0; i < totalt; i++) {
            try {
                varselService.sendVarsel(varslbarHendelse.getVarsler().get(i));
                log.info("Varsel {} av {} sendt for avtaleId={}", i, totalt, varslbarHendelse.getAvtaleId());
            } catch (Exception e) {
                log.error("Feil ved sending av varsel {} av {} for avtaleId={}", i, totalt, varslbarHendelse.getAvtaleId());
            }
        }
    }
}
