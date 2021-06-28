package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlar;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselStatus;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselResultatMottatt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
@Slf4j
public class RefusjonKlarConsumer extends AbstractAggregateRoot<SmsVarsel> {
    private final SmsVarselRepository smsVarselRepository;
    private final AvtaleRepository avtaleRepository;

    private static void loggFeil(RefusjonKlarVarselMelding resultatMelding) {
        log.warn("Finner ikke tilhørende avtale med tli refusjon med avtaleId={}, kan ikke sende varsel", resultatMelding.getRefusjonVarselId());
    }

    @KafkaListener(topics = VarselTopics.TILTAK_VARSEL)
    public void consume(RefusjonKlarVarselMelding refusjonKlarVarselMelding) {
        avtaleRepository.findById(refusjonKlarVarselMelding.getAvtaleId())
                .ifPresentOrElse(avtaleForVarsling -> registerEvent(new RefusjonKlar(avtaleForVarsling, null)), () -> loggFeil(refusjonKlarVarselMelding));
    }

    private void lagreStatus(SmsVarsel smsVarsel, SmsVarselStatus status) {
        log.info("Oppdatert SmsVarsel med smsVarselId={} til status={}", smsVarsel.getId(), status);
        smsVarsel.endreStatus(status);
        smsVarselRepository.save(smsVarsel);
    }
}
