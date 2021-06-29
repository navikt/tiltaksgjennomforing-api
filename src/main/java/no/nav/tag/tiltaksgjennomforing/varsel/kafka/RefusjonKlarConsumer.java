package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
@Slf4j
public class RefusjonKlarConsumer {
    private final AvtaleRepository avtaleRepository;

    private static void loggFeil(RefusjonKlarVarselMelding resultatMelding) {
        log.warn("Finner ikke tilhørende avtale med tli refusjon med avtaleId={}, kan ikke sende varsel", resultatMelding.getRefusjonVarselId());
    }

    @KafkaListener(topics = VarselTopics.TILTAK_VARSEL, properties = {"spring.json.value.default.type=no.nav.tag.tiltaksgjennomforing.varsel.kafka.RefusjonKlarVarselMelding"})
    public void consume(RefusjonKlarVarselMelding refusjonKlarVarselMelding) {
        UUID avtaleId = UUID.fromString(refusjonKlarVarselMelding.getAvtaleId());
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RuntimeException::new);
        avtale.refusjonKlar();
        avtaleRepository.save(avtale);
    }

}