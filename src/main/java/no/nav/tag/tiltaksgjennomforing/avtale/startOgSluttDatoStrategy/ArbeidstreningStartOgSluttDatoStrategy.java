package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangArbeidstreningException;

import java.time.LocalDate;

public class ArbeidstreningStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering);
        if (startDato != null && sluttDato != null && startDato.plusMonths(18).isBefore(sluttDato)) {
            throw new VarighetForLangArbeidstreningException();
        }
    }
}
