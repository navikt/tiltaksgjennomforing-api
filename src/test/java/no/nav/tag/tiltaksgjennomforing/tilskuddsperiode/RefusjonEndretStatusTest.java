package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefusjonEndretStatusTest {

    @Test
    public void skal_kunne_finne_riktig_tilskuddsperiode_og_lagre_status_uten_å_kaste_en_feil() throws JsonProcessingException {
        TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        when(tilskuddPeriodeRepository.findById(any())).thenReturn(Optional.of(tilskuddPeriode));

        ObjectMapper objectMapper = new ObjectMapper();
        RefusjonEndretStatusKafkaConsumer consumer = new RefusjonEndretStatusKafkaConsumer(
            tilskuddPeriodeRepository,
            objectMapper
        );
        RefusjonEndretStatusMelding melding = new RefusjonEndretStatusMelding(
            "1234",
            "1234",
            "1234",
            RefusjonStatus.UTBETALT,
            tilskuddPeriode.getId().toString()
        );
        consumer.refusjonEndretStatus(objectMapper.writeValueAsString(melding));

        verify(tilskuddPeriodeRepository).save(any());
    }

}
