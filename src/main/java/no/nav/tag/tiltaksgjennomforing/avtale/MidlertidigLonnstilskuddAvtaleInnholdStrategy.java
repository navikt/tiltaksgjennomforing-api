package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {


    private MidlertidigLonnstilskuddAvtaleBeregningStrategy beregningStrategy;

    public MidlertidigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        beregningStrategy = new MidlertidigLonnstilskuddAvtaleBeregningStrategy();
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
        beregningStrategy.reberegnTotal(avtale);
    }

    private void sjekktilskuddsprosentSats(EndreAvtale endreAvtale) {
        beregningStrategy.sjekktilskuddsprosentSats(endreAvtale.getLonnstilskuddProsent());
    }

    private void settTilskuddsprosentSats(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        final Integer sats = kvalifiseringsgruppe.finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60);
        avtaleInnhold.setLonnstilskuddProsent(sats);
    }

    private void regnUtDatoOgSumRedusert() {
       LocalDate datoForRedusertProsent = beregningStrategy.getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = beregningStrategy.regnUtRedusertLønnstilskudd(avtaleInnhold.getAvtale());
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }


    @Override
    public void endreSluttDato(LocalDate nySluttDato) {
        super.endreSluttDato(nySluttDato);
        regnUtDatoOgSumRedusert();
    }
}
