package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;

import java.time.LocalDate;

public class InkluderingstilskuddStartOgSluttdatoStrategy extends StartOgSluttdatoStrategy {

    public InkluderingstilskuddStartOgSluttdatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttdato(stillingstype, startDato, sluttDato);

        if (sluttDato == null) {
            return;
        }
        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (deltakerFnr != null && deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (startDato == null) {
            return;
        }
        boolean erNyInkluderingstilskudd = FeatureToggleHolder.get().isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD);
        if (erNyInkluderingstilskudd && startDato.getYear() != sluttDato.getYear()) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD_PAA_TVERS_AV_AAR);
        }
        if (!erNyInkluderingstilskudd && startDato.plusMonths(12).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD);
        }
    }
}
