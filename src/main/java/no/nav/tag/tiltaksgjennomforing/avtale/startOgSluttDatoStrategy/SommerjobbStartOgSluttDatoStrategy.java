package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class SommerjobbStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato);
        if (startDato != null) {
            if (startDato.isBefore(LocalDate.of(2021, 6, 1))) {
                throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_TIDLIG);
            }
        }
        if (sluttDato != null) {
            if (sluttDato.isAfter(LocalDate.of(2021, 8, 31))) {
                throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_SENT);
            }
        }
        if (startDato != null && sluttDato != null) {
            if (startDato.plusWeeks(4).isBefore(sluttDato)) {
                throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET);
            }
        }
    }
}
