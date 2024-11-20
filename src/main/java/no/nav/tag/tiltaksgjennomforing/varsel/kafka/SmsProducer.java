package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.varsel.Sms;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class SmsProducer {
    public void sendSmsVarselMeldingTilKafka(Sms sms) throws JsonProcessingException {}
}
