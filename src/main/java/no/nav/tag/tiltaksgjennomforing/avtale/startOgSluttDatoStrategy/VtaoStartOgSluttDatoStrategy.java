package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class VtaoStartOgSluttDatoStrategy extends StartOgSluttDatoStrategy {

    public VtaoStartOgSluttDatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttDato(startDato, sluttDato);

        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (sluttDato != null && deltakerFnr != null && deltakerFnr.erOver67ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_67_AAR);
        }
    }
}
