package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsVarselResultatConsumer {
    private final SmsVarselRepository smsVarselRepository;

    @KafkaListener(topics = Topics.SMS_VARSEL_RESULTAT)
    public void consume(SmsVarselResultatMelding resultatMelding) {
        smsVarselRepository.findById(resultatMelding.getSmsVarselId())
                .ifPresentOrElse(smsVarsel -> lagreStatus(smsVarsel, resultatMelding.getStatus()), () -> loggFeil(resultatMelding));
    }

    private static void loggFeil(SmsVarselResultatMelding resultatMelding) {
        log.warn("Finner ikke SmsVarsel med smsVarselId={} og kan ikke oppdatere til status={}", resultatMelding.getSmsVarselId(), resultatMelding.getStatus());
    }

    private void lagreStatus(SmsVarsel smsVarsel, SmsVarselStatus status) {
        log.info("Oppdatert SmsVarsel med smsVarselId={} til status={}", smsVarsel.getId(), status);
        smsVarsel.endreStatus(status);
        smsVarselRepository.save(smsVarsel);
    }
}
