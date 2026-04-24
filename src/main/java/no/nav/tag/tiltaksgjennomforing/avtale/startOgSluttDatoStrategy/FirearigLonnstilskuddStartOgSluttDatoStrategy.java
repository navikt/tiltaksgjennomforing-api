package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.time.Period;

public class FirearigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {

    private final LocalDate firearigOppstartsdato;

    public FirearigLonnstilskuddStartOgSluttDatoStrategy() {
        this.firearigOppstartsdato = FirearigLonnstilskuddProperties.getInstance().getDato();
    }

    public FirearigLonnstilskuddStartOgSluttDatoStrategy(LocalDate firearigOppstartsdato) {
        this.firearigOppstartsdato = firearigOppstartsdato;
    }

    @Override
    public void sjekkStartOgSluttDato(
        LocalDate startDato,
        LocalDate sluttDato,
        boolean erGodkjentForEtterregistrering,
        boolean erAvtaleInngått,
        Fnr deltakerFnr
    ) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(
            startDato,
            sluttDato,
            erGodkjentForEtterregistrering,
            erAvtaleInngått,
            deltakerFnr
        );
        if (startDato == null) {
            return;
        }
        if (startDato.isBefore(firearigOppstartsdato)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_TIDLIG_OPPSTART);
        }
        if (deltakerFnr != null && deltakerFnr.erOver30årFraOppstartDato(startDato)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }
        if (sluttDato == null) {
            return;
        }
        if (Period.between(startDato, sluttDato).getYears() >= 4) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_LANG_VARIGHET);
        }

        if (sluttDato.isAfter(LocalDate.of(2032,12,31))){
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_SEN_SLUTTDATO);
        }
    }
}
