package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class InkluderingstilskuddStartOgSluttDatoStrategy extends StartOgSluttDatoStrategy {

    public InkluderingstilskuddStartOgSluttDatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttDato(startDato, sluttDato);

        if (sluttDato == null) {
            return;
        }
        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (deltakerFnr != null && deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (startDato != null && startDato.plusMonths(12).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD);
        }
    }
}
