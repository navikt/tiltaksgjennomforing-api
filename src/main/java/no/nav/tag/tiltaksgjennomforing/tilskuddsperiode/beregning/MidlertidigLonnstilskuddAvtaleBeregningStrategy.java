package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.time.LocalDate;
public class MidlertidigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    public void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning) {
       super.endre(avtale,endreTilskuddsberegning);
        regnUtDatoOgSumRedusert(avtale);//TODO IKKE TESTET
        avtale.sendTilbakeTilBeslutter(); //TODO IKKE TESTET
        avtale.getTilskuddPeriode().stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET)
                .forEach(t -> t.setBeløp(beregnTilskuddsbeløpForPeriode(avtale,t.getStartDato(), t.getSluttDato())));


    }
    public void reberegnTotalIAvtale(Avtale avtale) {
        super.reberegnTotalIAvtale(avtale);
        regnUtDatoOgSumRedusert(avtale);
    }
    public void regnUtDatoOgSumRedusert(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        LocalDate datoForRedusertProsent = getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = regnUtRedusertLønnstilskudd(avtale);
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }


    public Integer regnUtRedusertLønnstilskudd(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        if (avtaleInnhold.getDatoForRedusertProsent() != null && avtaleInnhold.getLonnstilskuddProsent() != null) {
            return getSumLonnsTilskudd(avtaleInnhold.getSumLonnsutgifter(), avtaleInnhold.getLonnstilskuddProsent() - 10);
        } else {
            return null;
        }
    }

    public LocalDate getDatoForRedusertProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddprosent) {
        if (startDato == null || sluttDato == null || lonnstilskuddprosent == null) {
            return null;
        }
        if (lonnstilskuddprosent == 40) {
            if (startDato.plusMonths(6).minusDays(1).isBefore(sluttDato)) {
                return startDato.plusMonths(6);
            }

        } else if (lonnstilskuddprosent == 60) {
            if (startDato.plusYears(1).minusDays(1).isBefore(sluttDato)) {
                return startDato.plusYears(1);
            }
        }

        return null;
    }

    public void sjekktilskuddsprosentSats(Integer lonnstilskuddProsent) {
        if (lonnstilskuddProsent != null && (
                lonnstilskuddProsent != 40 && lonnstilskuddProsent != 60)) {
            throw new FeilLonnstilskuddsprosentException();
        }
    }

}
