package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;

class TilskuddPeriodeTest {
    @Test
    void behandle_flere_ganger__etter_godkjenning() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.godkjenn(TestData.enNavIdent());
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.godkjenn(TestData.enNavIdent()));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.avslå(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_FAKTA), "Faktafeil"));
    }

    @Test
    void behandle_flere_ganger__etter_avslag() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.avslå(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_FAKTA), "Faktafeil");
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.godkjenn(TestData.enNavIdent()));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.avslå(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_FAKTA), "Faktafeil"));
    }

    @Test
    void godkjenn_setter_riktige_felter() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        NavIdent beslutter = TestData.enNavIdent();
        tilskuddPeriode.godkjenn(beslutter);
        assertThat(tilskuddPeriode.getGodkjentAvNavIdent()).isEqualTo(beslutter);
        assertThat(tilskuddPeriode.getGodkjentTidspunkt()).isNotNull();
        assertThat(tilskuddPeriode.getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
    }

    @Test
    void avslå_setter_riktige_felter() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        NavIdent beslutter = TestData.enNavIdent();
        tilskuddPeriode.avslå(beslutter, EnumSet.of(Avslagsårsak.FEIL_I_FAKTA, Avslagsårsak.ANNET), "Feil i fakta");
        assertThat(tilskuddPeriode.getAvslåttAvNavIdent()).isEqualTo(beslutter);
        assertThat(tilskuddPeriode.getAvslåttTidspunkt()).isNotNull();
        assertThat(tilskuddPeriode.getStatus()).isEqualTo(TilskuddPeriodeStatus.AVSLÅTT);
        assertThat(tilskuddPeriode.getAvslagsårsaker()).contains(Avslagsårsak.FEIL_I_FAKTA, Avslagsårsak.ANNET);
    }

    @Test
    void avslå__uten_årsaker() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        NavIdent beslutter = TestData.enNavIdent();
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_INGEN_AVSLAGSAARSAKER, () -> tilskuddPeriode.avslå(beslutter, EnumSet.noneOf(Avslagsårsak.class), "Feil i fakta"));
    }

    @Test
    void avslå__uten_forklaring() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        NavIdent beslutter = TestData.enNavIdent();
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_AVSLAGSFORKLARING_PAAKREVD, () -> tilskuddPeriode.avslå(beslutter, EnumSet.of(Avslagsårsak.FEIL_I_REGELFORSTÅELSE), "   "));
    }
}