package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import java.time.LocalDate;

public class VarigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato);
    }
}
