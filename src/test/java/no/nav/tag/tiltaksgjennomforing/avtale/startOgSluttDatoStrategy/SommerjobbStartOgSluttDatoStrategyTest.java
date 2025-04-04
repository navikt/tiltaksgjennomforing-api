package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;

public class SommerjobbStartOgSluttDatoStrategyTest {

    @Test
    public void sjekkStartOgSluttDatoEtterregistreringFeilDatoForSommerjobb(){
        LocalDate avtaleStart = LocalDate.of(Now.localDate().minusYears(2).getYear(), 5,2);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().minusYears(2).getYear(),7,28);


        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_TIDLIG, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått));
    }

    @Test
    public void sjekkStartOgSluttDatoTilbakeITidUtenEtterregistreringInnenForFireUkerSommerjobbPeriode(){
        LocalDate avtaleStart = LocalDate.of(Now.localDate().minusYears(2).getYear(), 9,1);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().minusYears(2).getYear(),9,28);

        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_SENT, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått));

    }

    @Test
    public void sjekkStartOgSluttDatoTilbakeITidUtenEtterregistreringIKKEInnenForFireUkerSommerjobbPeriode(){
        LocalDate avtaleStart = LocalDate.of(Now.localDate().minusYears(2).getYear(), 8,31);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().minusYears(2).getYear(),10,1);


        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått));

    }

    @Test
    public void sjekkStartOgSluttDato(){
        LocalDate avtaleStart = LocalDate.of(Now.localDate().getYear(), 6,1);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().getYear(),6,20);


        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt, erGodkjentForEtterregistrering, erAvtaleInngått );
    }

    @Test
    public void avtaleSluttDatoErMerEnnFireUkerSent() {
        LocalDate avtaleStart = LocalDate.of(Now.localDate().getYear(),8,31);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().getYear(),9,29);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått));
    }

    @Test
    public void avtale_periode_kan_ikke_være_over_4_uker() {
        LocalDate avtaleStart = LocalDate.of(Now.localDate().plusYears(1).getYear(),6,1);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().plusYears(1).getYear(),6,29);
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt, false, false));
    }

    @Test
    public void avtale_periode_akkurat_4_uker() {
        LocalDate avtaleStart = LocalDate.of(Now.localDate().plusYears(1).getYear(),6,1);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().plusYears(1).getYear(),6,28);
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt, false, false);
    }

    @Test
        public void avtaleStartDatoErFørFørstJuni(){
        LocalDate avtaleStart = LocalDate.of(Now.localDate().getYear(),5,31);
        LocalDate avtaleSlutt = LocalDate.of(Now.localDate().getYear(),7,14);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_TIDLIG, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått));

    }
}

