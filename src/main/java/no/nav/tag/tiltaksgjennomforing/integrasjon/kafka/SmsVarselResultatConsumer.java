package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelseRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsVarselResultatConsumer {
    private final VarslbarHendelseRepository varslbarHendelseRepository;

    @KafkaListener(groupId = "smsVarselResultatConsumer", topics = Topics.SMS_VARSEL_RESULTAT)
    public void consume(SmsVarselResultatMelding resultatMelding) {
        varslbarHendelseRepository.finnForSmsVarselId(resultatMelding.getSmsVarselId())
                .ifPresentOrElse(hendelse -> {
                            hendelse.settStatusPaaSmsVarsel(resultatMelding.getSmsVarselId(), resultatMelding.getStatus());
                            varslbarHendelseRepository.save(hendelse);
                            log.info("Oppdaterer SmsVarsel med smsVarselId={} til status={}", resultatMelding.getSmsVarselId(), resultatMelding.getStatus());
                        },
                        () -> log.warn("Finner ikke SmsVarsel med smsVarselId={} og kan ikke oppdatere til status={}", resultatMelding.getSmsVarselId(), resultatMelding.getStatus()));
    }
}
