package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.VTAO;

class VtaoStartOgSluttDatoStrategyTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_Vtao() {
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(Fnr.generer(1952,8,12), TestData.etBedriftNr(), VTAO), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        VtaoStartOgSluttdatoStrategy vtaoStartOgSluttDatoStrategy = new VtaoStartOgSluttdatoStrategy(avtale);
        assertFeilkode(Feilkode.DELTAKER_67_AAR, () -> vtaoStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt));
    }

    @Test
    public void startdato_er_for_sen_for_Vtao() {
        LocalDate avtaleStart = LocalDate.of(2026, 9, 1);
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), VTAO), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setGodkjentForEtterregistrering(true); // Unngå etterregistreringsbehov
        VtaoStartOgSluttdatoStrategy vtaoStartOgSluttDatoStrategy = new VtaoStartOgSluttdatoStrategy(avtale);
        assertFeilkode(Feilkode.FOR_SEN_STARTDATO_VTAO, () -> vtaoStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt));
    }

    @Test
    public void startdato_er_ikke_for_sen_for_Vtao() {
        LocalDate avtaleStart = LocalDate.of(2026, 8, 31);
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), VTAO), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setGodkjentForEtterregistrering(true); // Unngå etterregistreringsbehov
        VtaoStartOgSluttdatoStrategy vtaoStartOgSluttDatoStrategy = new VtaoStartOgSluttdatoStrategy(avtale);
        vtaoStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt);
    }

    @Test
    public void inngått_avtale_tillater_startdato_som_ellers_er_for_sen_for_Vtao() {
        LocalDate avtaleStart = LocalDate.of(2026, 9, 1);
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), VTAO), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setGodkjentForEtterregistrering(true); // Unngå etterregistreringsbehov
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.instant());
        VtaoStartOgSluttdatoStrategy vtaoStartOgSluttDatoStrategy = new VtaoStartOgSluttdatoStrategy(avtale);

        vtaoStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt);
    }
}
