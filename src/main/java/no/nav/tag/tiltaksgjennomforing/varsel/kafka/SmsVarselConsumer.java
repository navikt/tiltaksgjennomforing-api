package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsVarselConsumer {
    private final VarselService varselService;
    private final SmsVarselResultatProducer resultatProducer;

    @KafkaListener(groupId = "smsVarselConsumer", clientIdPrefix = "smsVarselConsumer", topics = Topics.SMS_VARSEL)
    public void consume(SmsVarselMelding varselMelding) {
        SmsVarselResultatMelding resultatMelding = utfoerVarsling(varselMelding);
        resultatProducer.sendSmsVarselResultatMeldingTilKafka(resultatMelding);
    }

    private SmsVarselResultatMelding utfoerVarsling(SmsVarselMelding varselMelding) {
        try {
            varselService.sendVarsel(varselMelding.getIdentifikator(), varselMelding.getTelefonnummer(), varselMelding.getMeldingstekst());
            return SmsVarselResultatMelding.sendt(varselMelding);
        } catch (TiltaksgjennomforingException e) {
            return SmsVarselResultatMelding.feil(varselMelding);
        }
    }
}
