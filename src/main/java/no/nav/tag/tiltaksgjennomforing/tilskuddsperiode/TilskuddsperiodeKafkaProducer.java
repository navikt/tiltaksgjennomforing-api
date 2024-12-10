package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class TilskuddsperiodeKafkaProducer {
    public void publiserTilskuddsperiodeGodkjentMelding(TilskuddsperiodeGodkjentMelding melding) {}

    public void publiserTilskuddsperiodeAnnullertMelding(TilskuddsperiodeAnnullertMelding melding) {}
}
