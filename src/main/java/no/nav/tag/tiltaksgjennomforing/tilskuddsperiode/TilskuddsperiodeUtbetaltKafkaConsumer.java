package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class TilskuddsperiodeUtbetaltKafkaConsumer {
    private final AvtaleRepository avtaleRepository;
    private final ObjectMapper objectMapper;

    public TilskuddsperiodeUtbetaltKafkaConsumer(AvtaleRepository avtaleRepository, ObjectMapper objectMapper) {
        this.avtaleRepository = avtaleRepository;
        this.objectMapper = objectMapper;
    }

    //TODO: test for denne og frontend implementasjon
    @KafkaListener(topics = Topics.REFUSJON_GODKJENT)
    public void tilskuddsperiodeUtbetalt(String jsonMelding) throws JsonProcessingException {
        RefusjonGodkjentMelding melding = objectMapper.readValue(jsonMelding, RefusjonGodkjentMelding.class);
        Avtale avtale = avtaleRepository.findById(melding.getAvtaleId()).orElseThrow();
        TilskuddPeriode tilskuddPeriode = avtale.getTilskuddPeriode().stream().filter(it -> it.getId().equals(melding.getTilskuddsperiodeId())).findFirst().orElseThrow();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UTBETALT);
        avtaleRepository.save(avtale);
    }
}
