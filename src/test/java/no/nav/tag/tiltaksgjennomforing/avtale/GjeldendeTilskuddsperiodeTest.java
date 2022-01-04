package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter;
import static org.assertj.core.api.Assertions.assertThat;

public class GjeldendeTilskuddsperiodeTest {

    @Test
    public void godkjenner_og_neste_kan_behandles() {
        Now.fixedDate(LocalDate.of(2021, 4, 20));
        LocalDate avtaleStart = Now.localDate().minusMonths(3).plusDays(13);
        LocalDate avtaleSlutt = Now.localDate().plusMonths(6);

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfyltMedGodkjentForEtterregistrering(avtaleStart, avtaleSlutt);

        assertThat(avtale.tilskuddsperiode(0)).isEqualTo(avtale.gjeldendeTilskuddsperiode());

        avtale.godkjennTilskuddsperiode(TestData.enNavIdent2(), "0000");
        assertThat(avtale.tilskuddsperiode(1)).isEqualTo(avtale.gjeldendeTilskuddsperiode());

        avtale.godkjennTilskuddsperiode(TestData.enNavIdent2(), "0000");
        assertThat(avtale.tilskuddsperiode(1)).isEqualTo(avtale.gjeldendeTilskuddsperiode());

        Now.resetClock();
    }

    // 2
    @Test
    public void en_periode() {
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = Now.localDate();
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        assertThat(avtale.tilskuddsperiode(0)).isEqualTo(avtale.gjeldendeTilskuddsperiode());
    }

    // 5
    @Test
    public void frem_i_tid() {
        LocalDate avtaleStart = Now.localDate().plusDays(15);
        LocalDate avtaleSlutt = Now.localDate().plusMonths(8);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        assertThat(avtale.tilskuddsperiode(0)).isEqualTo(avtale.gjeldendeTilskuddsperiode());
    }

    // 6
    @Test
    public void første_avslått__neste_kan_behandles() {
        Now.fixedDate(LocalDate.of(2021, 4, 20));
        LocalDate avtaleStart = Now.localDate().minusMonths(6);
        LocalDate avtaleSlutt = Now.localDate().plusMonths(8);

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfyltMedGodkjentForEtterregistrering(avtaleStart, avtaleSlutt);

        avtale.avslåTilskuddsperiode(TestData.enNavIdent2(), EnumSet.of(Avslagsårsak.ANNET), "Forklaring");
        assertThat(avtale.tilskuddsperiode(0)).isEqualTo(avtale.gjeldendeTilskuddsperiode());
    }

    // 7
    @Test
    public void første_avslått__neste_kan_ikke_behandles() {
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = Now.localDate().plusMonths(8);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        avtale.avslåTilskuddsperiode(TestData.enNavIdent2(), EnumSet.of(Avslagsårsak.ANNET), "Forklaring");
        assertThat(avtale.tilskuddsperiode(0)).isEqualTo(avtale.gjeldendeTilskuddsperiode());
    }

    // 8
    @Test
    public void godkjenner_og_neste_kan_ikke_behandles() {
        Now.fixedDate(LocalDate.of(2021, 11, 30));
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = Now.localDate().plusMonths(6);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        assertThat(avtale.gjeldendeTilskuddsperiode().getStartDato()).isEqualTo(avtaleStart);
        avtale.godkjennTilskuddsperiode(TestData.enNavIdent2(), avtale.getEnhetGeografisk());
        assertThat(avtale.gjeldendeTilskuddsperiode().getStartDato()).isEqualTo(avtaleStart);
        assertThat(avtale.gjeldendeTilskuddsperiode().getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        Now.resetClock();
    }


}
