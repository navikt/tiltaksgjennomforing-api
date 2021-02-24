package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter;
import static org.assertj.core.api.Assertions.assertThat;

public class GjeldendeTilskuddsperiodeTest {

    // 1 og 4
    @Test
    public void godkjenner_og_neste_kan_behandles() {
        LocalDate avtaleStart = LocalDate.now().minusMonths(3).plusDays(13);
        LocalDate avtaleSlutt = LocalDate.now().plusMonths(6);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);

        // 4
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(0);

        // 1
        avtale.godkjennTilskuddsperiode(TestData.enNavIdent());
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(1);

        // 3
        avtale.godkjennTilskuddsperiode(TestData.enNavIdent());
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(2);
    }

    // 2
    @Test
    public void en_periode() {
        LocalDate avtaleStart = LocalDate.now();
        LocalDate avtaleSlutt = LocalDate.now();
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(0);
    }

    // 5
    @Test
    public void frem_i_tid() {
        LocalDate avtaleStart = LocalDate.now().plusDays(15);
        LocalDate avtaleSlutt = LocalDate.now().plusMonths(8);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(0);
    }

    // 6
    @Test
    public void første_avslått__neste_kan_behandles() {
        LocalDate avtaleStart = LocalDate.now().minusMonths(6);
        LocalDate avtaleSlutt = LocalDate.now().plusMonths(8);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        avtale.avslåTilskuddsperiode(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.ANNET), "Forklaring");
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(0);
    }

    // 7
    @Test
    public void første_avslått__neste_kan_ikke_behandles() {
        LocalDate avtaleStart = LocalDate.now();
        LocalDate avtaleSlutt = LocalDate.now().plusMonths(8);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        avtale.avslåTilskuddsperiode(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.ANNET), "Forklaring");
        assertThat(avtale.getTilskuddPeriode().indexOf(avtale.gjeldendeTilskuddsperiode())).isEqualTo(0);
    }

    // 8
    @Test
    public void godkjenner_og_neste_kan_ikke_behandles() {
        LocalDate avtaleStart = LocalDate.now();
        LocalDate avtaleSlutt = LocalDate.now().plusMonths(6);
        Avtale avtale = enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);
        assertThat(avtale.gjeldendeTilskuddsperiode().getStartDato()).isEqualTo(avtaleStart);
        avtale.godkjennTilskuddsperiode(TestData.enNavIdent());
        assertThat(avtale.gjeldendeTilskuddsperiode().getStartDato()).isEqualTo(avtaleStart);
        assertThat(avtale.gjeldendeTilskuddsperiode().getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
    }


}
