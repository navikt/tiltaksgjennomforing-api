package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class VtaoStartOgSluttdatoStrategy extends StartOgSluttdatoStrategy {
    private static final LocalDate SISTE_MULIGE_STARTDATO = LocalDate.of(2026, 8, 31);

    public VtaoStartOgSluttdatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttdato(stillingstype, startDato, sluttDato);

        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (sluttDato != null && deltakerFnr != null && deltakerFnr.erOver67ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_67_AAR);
        }
        if (!avtale.erAvtaleInngått() && startDato != null && startDato.isAfter(SISTE_MULIGE_STARTDATO)) {
            throw new FeilkodeException(Feilkode.FOR_SEN_STARTDATO_VTAO);
        }
    }
}
