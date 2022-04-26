package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMentorException;

import java.time.LocalDate;

public class InkluderingsTiltakStartOgSluttDatoStrategy  implements StartOgSluttDatoStrategy  {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering, boolean erAvtaleInngått) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått);
        if (startDato != null && sluttDato != null && startDato.plusMonths(36).isBefore(sluttDato)) {
            throw new VarighetForLangMentorException();
        }
    }
}
