package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Optional;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

class RefusjonEndretBetalingsstatusTest {

    @Test
    public void skal_kunne_finne_riktig_tilskuddsperiode_og_lagre_status_uten_å_kaste_en_feil() throws JsonProcessingException {
        // GITT
        TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        when(tilskuddPeriodeRepository.findById(any())).thenReturn(Optional.of(tilskuddPeriode));

        // NÅR
        RefusjonEndretBetalingsstatusKafkaConsumer consumer = new RefusjonEndretBetalingsstatusKafkaConsumer(tilskuddPeriodeRepository, mapper);

        consumer.refusjonEndretBetalingsstatus(mapper.writeValueAsString(new RefusjonEndretBetalingsstatusMelding("1234", "1234", "1234", 10000, 1234, "1243", TilskuddsperiodeUtbetaltStatus.UTBETALT, Now.localDate(), tilskuddPeriode.getId().toString())));

        // SÅ
        verify(tilskuddPeriodeRepository).save(any());
    }

}
