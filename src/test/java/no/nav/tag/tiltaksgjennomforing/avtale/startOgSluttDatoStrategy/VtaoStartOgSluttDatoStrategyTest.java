package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;

class VtaoStartOgSluttDatoStrategyTest {

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_Inkluderingstilskudd() {
        Fnr deltakerFnr = Fnr.generer(1952,8,12);
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        VtaoStartOgSluttDatoStrategy vtaoStartOgSluttDatoStrategy = new VtaoStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.DELTAKER_67_AAR, () -> vtaoStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr));
    }
}
