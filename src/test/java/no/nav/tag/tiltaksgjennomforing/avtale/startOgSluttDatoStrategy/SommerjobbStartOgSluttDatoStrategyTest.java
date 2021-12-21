package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;

public class SommerjobbStartOgSluttDatoStrategyTest {

    @Test
    public void sjekkStartOgSluttDato(){
        LocalDate avtaleStart = LocalDate.of(2021,8,31);
        LocalDate avtaleSlutt = LocalDate.of(2021,9,28);
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt, erGodkjentForEtterregistrering);
    }

    @Test
    public void avtaleSluttDatoErMerEnnFireUkerSent() {
        LocalDate avtaleStart = LocalDate.of(2021,8,31);
        LocalDate avtaleSlutt = LocalDate.of(2021,9,29);
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering));
    }

    @Test
        public void avtaleStartDatoErFørFørstJuni(){
        LocalDate avtaleStart = LocalDate.of(2021,5,31);
        LocalDate avtaleSlutt = LocalDate.of(2021,7,14);
        boolean erGodkjentForEtterregistrering = true;
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_TIDLIG, () -> sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering));

    }
}

