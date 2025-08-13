package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class SommerjobbStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering, boolean erAvtaleInngått, Fnr deltakerFnr) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr);
        if (startDato != null) {
                if (startDato.isBefore(LocalDate.of(startDato.getYear(), 6, 1)) ) {
                    throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_TIDLIG);
                }
                if (startDato.isAfter(LocalDate.of(startDato.getYear(), 8, 31))) {
                    throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_SENT);
                }
                if (deltakerFnr.erOver30årFraOppstartDato(startDato)) {
                    throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL_FRA_OPPSTARTDATO);
                }
        }
      if (startDato != null && sluttDato != null) {
        if (startDato.plusWeeks(4).minusDays(1).isBefore(sluttDato)) {
          throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET);
        }else{
          if (sluttDato.isBefore(LocalDate.of(sluttDato.getYear(), 6, 1)) ) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_TIDLIG);
          }
          if (sluttDato.isAfter(LocalDate.of(sluttDato.getYear(), 9, 27))) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_SENT);
          }
        }
      }
    }
}
