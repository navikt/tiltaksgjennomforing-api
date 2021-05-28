package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMidlertidigLonnstilskuddException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato);
        if (startDato != null && sluttDato != null && startDato.plusMonths(24).isBefore(sluttDato)) {
            throw new VarighetForLangMidlertidigLonnstilskuddException();
        }
    }
}
