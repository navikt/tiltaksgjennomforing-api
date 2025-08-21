package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangArbeidstreningException;

import java.time.LocalDate;

public class ArbeidstreningStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering, boolean erAvtaleInngått, Fnr deltakerFnr) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr);
        if (startDato != null && sluttDato != null && startDato.plusMonths(18).minusDays(1).isBefore(sluttDato)) {
            throw new VarighetForLangArbeidstreningException();
        }
        if(sluttDato == null){
            return;
        }
        if (deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
    }
}
