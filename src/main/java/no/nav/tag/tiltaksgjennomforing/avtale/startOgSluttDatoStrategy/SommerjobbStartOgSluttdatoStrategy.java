package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class SommerjobbStartOgSluttdatoStrategy extends StartOgSluttdatoStrategy {


    public SommerjobbStartOgSluttdatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttdato(stillingstype, startDato, sluttDato);

        if (startDato == null){
            return;
        }
        if (startDato.isBefore(LocalDate.of(startDato.getYear(), 6, 1)) ) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_TIDLIG);
        }
        if (startDato.isAfter(LocalDate.of(startDato.getYear(), 8, 31))) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_SENT);
        }
        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (deltakerFnr != null && deltakerFnr.erOver30årFraOppstartDato(startDato)) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }
        if (sluttDato == null){
            return;
        }
        if (startDato.plusWeeks(4).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET);
        }
        if (sluttDato.isBefore(LocalDate.of(sluttDato.getYear(), 6, 1)) ) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_TIDLIG);
        }
        if (sluttDato.isAfter(LocalDate.of(sluttDato.getYear(), 9, 27))) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_SENT);
        }
    }
}
