package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class FirearigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering, boolean erAvtaleInngått, Fnr deltakerFnr) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr);
        if (startDato == null){
            return;
        }
        if (deltakerFnr != null && deltakerFnr.erOver30årFraOppstartDato(startDato)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }
        if (sluttDato == null){
            return;
        }
        if (startDato.plusYears(4).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_LANG_VARIGHET);
        }
    }
}
