package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.junit.Test;

import java.time.LocalDate;

public class SommerjobbStartOgSluttDatoStrategyTest {

    @Test
    public void sjekkStartOgSluttDato(){
        LocalDate avtaleStart = LocalDate.of(2021,8,31);
        LocalDate avtaleSlutt = LocalDate.of(2021,9,28);
        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt);
    }

    @Test(expected = FeilkodeException.class)
    public void avtaleSluttDatoErMerEnnFireUkerSent() {
        LocalDate avtaleStart = LocalDate.of(2021,8,31);
        LocalDate avtaleSlutt = LocalDate.of(2021,9,29);

        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt);
    }

    @Test(expected = FeilkodeException.class)
        public void avtaleStartDatoErFørFørstJuni(){
        LocalDate avtaleStart = LocalDate.of(2021,5,31);
        LocalDate avtaleSlutt = LocalDate.of(2021,7,14);

        SommerjobbStartOgSluttDatoStrategy sommerjobbStartOgSluttDatoStrategy = new SommerjobbStartOgSluttDatoStrategy();
        sommerjobbStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt);

    }
}

