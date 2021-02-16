package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TilskuddsperiodeGodkjentMeldingTest {

    @Test
    void skal_kun_sende_godkjente_tilskudd_perioder() {

        // GIVEN
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
        UUID tilskuddsperiodeId = UUID.randomUUID();
        tilskuddPeriode.setId(tilskuddsperiodeId);
        tilskuddPeriode.setBeløp(1000);
        tilskuddPeriode.setStartDato(LocalDate.now().minusDays(1));
        tilskuddPeriode.setSluttDato(LocalDate.now());

        avtale.setTilskuddPeriode(List.of(tilskuddPeriode));

        // WHEN
        TilskuddsperiodeGodkjentMelding tilskuddMelding = TilskuddsperiodeGodkjentMelding.fraAvtale(avtale);

        // THEN
        assertThat(tilskuddMelding.getTilskuddsperiodeId()).isEqualTo(tilskuddsperiodeId);
    }
}