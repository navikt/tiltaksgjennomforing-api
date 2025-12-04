package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;

class TilskuddPeriodeTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    void behandle_flere_ganger__etter_godkjenning() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.godkjenn(TestData.enNavIdent(), TestData.ENHET_GEOGRAFISK.getVerdi());
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.godkjenn(TestData.enNavIdent(), TestData.ENHET_GEOGRAFISK.getVerdi()));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.avslå(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_FAKTA), "Faktafeil"));
    }

    @Test
    void behandle_flere_ganger__etter_avslag() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.avslå(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_FAKTA), "Faktafeil");
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.godkjenn(TestData.enNavIdent(), TestData.ENHET_GEOGRAFISK.getVerdi()));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET, () -> tilskuddPeriode.avslå(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_FAKTA), "Faktafeil"));
    }

    @Test
    void godkjenn_setter_riktige_felter() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        NavIdent beslutter = TestData.enNavIdent();
        tilskuddPeriode.godkjenn(beslutter, TestData.ENHET_GEOGRAFISK.getVerdi());
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

    @Test
    void sjekker_utbetalt_status() {
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.setRefusjonStatus(RefusjonStatus.UTBETALT);
        assertThat(tilskuddPeriode.erUtbetalt()).isTrue();
    }

    static final Integer NESTE_AAR = 2027;
    static final Integer NAAVAERENDE_AAR = NESTE_AAR - 1;

    @Test
    void kan_ikke_beslutte_for_neste_aar() {
        // Første tilskuddsperiode kan alltid godkjennes tidligere enn 3 mnd før startdato,
        // men det er ikke ønskelig å åpne for dette for tilskuddsperioder som starter "neste år" grunnet
        // uavklart budsjett.
        Now.fixedDate(LocalDate.of(NAAVAERENDE_AAR, 1, 1));
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UBEHANDLET);

        tilskuddPeriode.setStartDato(LocalDate.of(NAAVAERENDE_AAR - 1, 1, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isTrue();

        tilskuddPeriode.setStartDato(LocalDate.of(NAAVAERENDE_AAR, 12, 31));
        assertThat(tilskuddPeriode.kanBehandles()).isTrue();

        tilskuddPeriode.setStartDato(LocalDate.of(NESTE_AAR, 1, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        tilskuddPeriode.setStartDato(LocalDate.of(NESTE_AAR, 6, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        tilskuddPeriode.setStartDato(LocalDate.of(NESTE_AAR + 1, 6, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        Now.resetClock();
    }

    @Test
    void kan_ikke_godkjenne_for_neste_aar() {
        // Alle tilskuddsperioder etter første kan godkjennes innen 3 mnd før startdato,
        // men det er ikke ønskelig å åpne for dette for tilskuddsperioder som starter "neste år" grunnet
        // uavklart budsjett.
        Now.fixedDate(LocalDate.of(NAAVAERENDE_AAR, 9, 1));
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
        tilskuddPeriode.setLøpenummer(2);

        tilskuddPeriode.setStartDato(LocalDate.of(NAAVAERENDE_AAR - 1, 1, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isTrue();

        tilskuddPeriode.setStartDato(LocalDate.of(NAAVAERENDE_AAR, 11, 30));
        assertThat(tilskuddPeriode.kanBehandles()).isTrue();

        tilskuddPeriode.setStartDato(LocalDate.of(NAAVAERENDE_AAR, 12, 31));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        tilskuddPeriode.setStartDato(LocalDate.of(NESTE_AAR, 1, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        tilskuddPeriode.setStartDato(LocalDate.of(NESTE_AAR, 6, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        tilskuddPeriode.setStartDato(LocalDate.of(NESTE_AAR + 1, 6, 1));
        assertThat(tilskuddPeriode.kanBehandles()).isFalse();

        Now.resetClock();
    }

    @Test
    @Disabled("Tester kun midlertidig sperre for å ikke kunne godkjenne tilskudd for neste år.")
    void godkjenn__skal_ikke_kunne_godkjenne_neste_års_tilskuddsperiode() {
        //TODO: Dette er en test av en midlertidig sperre.
        Now.fixedDate(LocalDate.of(2022, 10, 15));
        TilskuddPeriode tilskuddPeriode = TestData.enTilskuddPeriode();
        tilskuddPeriode.setStartDato(LocalDate.of(2023, 1, 1));
        tilskuddPeriode.setSluttDato(LocalDate.of(2023, 1, 31));

        assertFeilkode(Feilkode.TILSKUDDSPERIODE_BEHANDLE_FOR_TIDLIG, () -> tilskuddPeriode.godkjenn(TestData.enNavIdent(), TestData.ENHET_GEOGRAFISK.getVerdi()));

        Now.fixedDate(LocalDate.of(2022, 12, 15));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_BEHANDLE_FOR_TIDLIG, () -> tilskuddPeriode.godkjenn(TestData.enNavIdent(), TestData.ENHET_GEOGRAFISK.getVerdi()));

        Now.fixedDate(LocalDate.of(2023, 1, 1));
        tilskuddPeriode.godkjenn(TestData.enNavIdent(), TestData.ENHET_GEOGRAFISK.getVerdi());

        Now.resetClock();
    }
}
