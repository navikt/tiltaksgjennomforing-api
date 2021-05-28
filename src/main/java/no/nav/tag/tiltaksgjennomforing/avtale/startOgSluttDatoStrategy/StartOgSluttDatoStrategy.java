package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public interface StartOgSluttDatoStrategy {
    default void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.isAfter(sluttDato)) {
            throw new FeilkodeException(Feilkode.START_ETTER_SLUTT);
        }
    }
}
