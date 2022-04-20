package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
public class TilskuddsperiodeUtbetaltKafkaConsumer {
    private final AvtaleRepository avtaleRepository;
    private final ObjectMapper objectMapper;

    public TilskuddsperiodeUtbetaltKafkaConsumer(AvtaleRepository avtaleRepository, ObjectMapper objectMapper) {
        this.avtaleRepository = avtaleRepository;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = Topics.REFUSJON_GODKJENT, containerFactory = "refusjonContainerFactory")
    public void tilskuddsperiodeUtbetalt(String jsonMelding) throws JsonProcessingException {
        RefusjonGodkjentMelding melding = objectMapper.readValue(jsonMelding, RefusjonGodkjentMelding.class);
        Avtale avtale = avtaleRepository.findById(melding.getAvtaleId()).orElseThrow();
        avtale.setTilskuddsperiodeUtbetalt(melding.getTilskuddsperiodeId());
        avtaleRepository.save(avtale);
    }
}
