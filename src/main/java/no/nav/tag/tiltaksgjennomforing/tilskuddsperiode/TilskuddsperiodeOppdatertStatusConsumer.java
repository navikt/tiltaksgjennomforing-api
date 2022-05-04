package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
@Slf4j
public class TilskuddsperiodeOppdatertStatusConsumer {
    private final AvtaleRepository avtaleRepository;

    @KafkaListener(topics = Topics.TILSKUDDSPERIODE_OPPDATERT_STATUS, containerFactory = "varselContainerFactory")
    public void consume(TilskuddsperiodeOppdatertStatusMelding tilskuddsperiodeOppdatertStatusMelding) {
        log.info("Mottatt oppdatert status melding for tilskuddsperiode {} med ny status {}", tilskuddsperiodeOppdatertStatusMelding.getTilskuddsperiodeId(), tilskuddsperiodeOppdatertStatusMelding.getStatus());
        Avtale avtale = avtaleRepository.findById(tilskuddsperiodeOppdatertStatusMelding.getAvtaleId()).orElseThrow(RuntimeException::new);

        try {
            log.info("Forsøker å oppdatere status på tilskuddsperiode {}", tilskuddsperiodeOppdatertStatusMelding.getTilskuddsperiodeId());
            avtale.oppdaterTilskuddsperiodestatus(tilskuddsperiodeOppdatertStatusMelding.getTilskuddsperiodeId(), tilskuddsperiodeOppdatertStatusMelding.getStatus());
            avtaleRepository.save(avtale);
            log.info("Oppdatering av status for tilskuddsperiode {} til ny status {} vellykket", tilskuddsperiodeOppdatertStatusMelding.getTilskuddsperiodeId(), tilskuddsperiodeOppdatertStatusMelding.getStatus());

        } catch (Exception e) {
            log.warn("Kunne ikke oppdatere status for tilskuddsperiode {}", tilskuddsperiodeOppdatertStatusMelding.getTilskuddsperiodeId());
            throw e;
        }
    }
}
