package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy.MidlertidigTilskuddsperiodeBeregningStrategi;
import no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy.TilskuddsperiodeBeregningStrategi;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {


    private MidlertidigTilskuddsperiodeBeregningStrategi midlertidigTilskuddsperiodeBeregningStrategi;

    public MidlertidigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        midlertidigTilskuddsperiodeBeregningStrategi = (MidlertidigTilskuddsperiodeBeregningStrategi) TilskuddsperiodeBeregningStrategi.create(this.avtaleInnhold.getAvtale().getTiltakstype());
    }

    @Override
    public void endreAvtaleInnholdMedKvalifiseringsgruppe(EndreAvtale endreAvtale, Kvalifiseringsgruppe kvalifiseringsgruppe) {
        if (kvalifiseringsgruppe != null) {
            settTilskuddsprosentSats(kvalifiseringsgruppe);
            this.endre(endreAvtale);
        } else {
            sjekktilskuddsprosentSats(endreAvtale);
            super.endre(endreAvtale);
        }
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        sjekktilskuddsprosentSats(endreAvtale);
        super.endre(endreAvtale);
    }

    @Override
    public void regnUtTotalLonnstilskudd() {
        Avtale avtale = this.avtaleInnhold.getAvtale();
        TilskuddsperiodeBeregningStrategi.create(avtale.getTiltakstype()).total(avtale);
    }

    private void sjekktilskuddsprosentSats(EndreAvtale endreAvtale) {
        midlertidigTilskuddsperiodeBeregningStrategi.sjekktilskuddsprosentSats(endreAvtale.getLonnstilskuddProsent());
    }

    private void settTilskuddsprosentSats(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        final Integer sats = kvalifiseringsgruppe.finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60);
        avtaleInnhold.setLonnstilskuddProsent(sats);
    }

    private void regnUtDatoOgSumRedusert() {
        LocalDate datoForRedusertProsent = getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = regnUtRedusertLønnstilskudd();
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }

    private Integer regnUtRedusertLønnstilskudd() {
        if (avtaleInnhold.getDatoForRedusertProsent() != null && avtaleInnhold.getLonnstilskuddProsent() != null) {
            return getSumLonnsTilskudd(avtaleInnhold.getSumLonnsutgifter(), avtaleInnhold.getLonnstilskuddProsent() - 10);
        } else {
            return null;
        }
    }

    private LocalDate getDatoForRedusertProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddprosent) {
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

    @Override
    public void endreSluttDato(LocalDate nySluttDato) {
        super.endreSluttDato(nySluttDato);
        regnUtDatoOgSumRedusert();
    }
}
