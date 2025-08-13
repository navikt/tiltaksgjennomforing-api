package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class InkluderingstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering, boolean erAvtaleInngått, Fnr deltakerFnr) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr);
        if (startDato != null && sluttDato != null && startDato.plusMonths(12).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD);
        }
        if (deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
    }
}
