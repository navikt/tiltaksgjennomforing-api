package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Optional;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.TilskuddsperiodeUtbetaltKafkaConsumer;
import org.junit.jupiter.api.Test;

class TilskuddsperiodeUtbetaltKafkaConsumerTest {

  @Test
  public void skal_kunne_finne_riktig_tilskuddsperiode_og_lagre_status_uten_å_kaste_en_feil() throws JsonProcessingException {
    // GITT
    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    when(avtaleRepository.findById(any())).thenReturn(Optional.of(avtale));

    // NÅR
    TilskuddsperiodeUtbetaltKafkaConsumer consumer = new TilskuddsperiodeUtbetaltKafkaConsumer(avtaleRepository,mapper);
    consumer.tilskuddsperiodeUtbetalt(mapper.writeValueAsString(new RefusjonGodkjentMelding(avtale.getId(),avtale.getTilskuddPeriode().first().getId())));

    // SÅ
    verify(avtaleRepository).save(any());
  }

}