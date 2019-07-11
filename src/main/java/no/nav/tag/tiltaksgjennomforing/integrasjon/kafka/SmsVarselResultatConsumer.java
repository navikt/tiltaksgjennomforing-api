package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsVarselResultatConsumer {
    private final JdbcTemplate jdbcTemplate;

    @KafkaListener(groupId = "smsVarselResultatConsumer", clientIdPrefix = "smsVarselResultatConsumer", topics = Topics.SMS_VARSEL_RESULTAT)
    public void consume(SmsVarselResultatMelding resultatMelding) {
        // Bryter prinsippet om aggregate root her ved å gå direkte på sms_varsel og ikke bruke repository til varslbar_hendelse.
        // Grunnen til at sms_varsel oppdateres direkte er å unngå situasjon der resultat av to SMS-varslinger kommer inn fra Kafka samtidig,
        // og at den andre overskriver den første. Mulig at dette bør løses på annen måte.
        int oppdaterteRader = jdbcTemplate.update("update sms_varsel set status = ? where id = ?", resultatMelding.getStatus(), resultatMelding.getSmsVarselId());
        if (oppdaterteRader == 1) {
            log.info("Oppdatert SmsVarsel med smsVarselId={} til status={}", resultatMelding.getSmsVarselId(), resultatMelding.getStatus());
        } else {
            log.warn("Finner ikke SmsVarsel med smsVarselId={} og kan ikke oppdatere til status={}", resultatMelding.getSmsVarselId(), resultatMelding.getStatus());
        }
    }
}
