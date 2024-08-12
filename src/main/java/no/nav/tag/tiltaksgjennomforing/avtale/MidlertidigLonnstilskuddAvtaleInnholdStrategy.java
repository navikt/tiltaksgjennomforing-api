package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {


    private MidlertidigLonnstilskuddAvtaleBeregningStrategy midlertidigLonnstilskuddAvtaleBeregningStrategy;

    public MidlertidigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        midlertidigLonnstilskuddAvtaleBeregningStrategy = new MidlertidigLonnstilskuddAvtaleBeregningStrategy();
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
        midlertidigLonnstilskuddAvtaleBeregningStrategy.reberegnTotalIAvtale(avtale);
    }

    private void sjekktilskuddsprosentSats(EndreAvtale endreAvtale) {
        midlertidigLonnstilskuddAvtaleBeregningStrategy.sjekktilskuddsprosentSats(endreAvtale.getLonnstilskuddProsent());
    }

    private void settTilskuddsprosentSats(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        final Integer sats = kvalifiseringsgruppe.finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60);
        avtaleInnhold.setLonnstilskuddProsent(sats);
    }

    private void regnUtDatoOgSumRedusert() {
       LocalDate datoForRedusertProsent = midlertidigLonnstilskuddAvtaleBeregningStrategy.getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = midlertidigLonnstilskuddAvtaleBeregningStrategy.regnUtRedusertLønnstilskudd(avtaleInnhold.getAvtale());
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }


    @Override
    public void endreSluttDato(LocalDate nySluttDato) {
        super.endreSluttDato(nySluttDato);
        regnUtDatoOgSumRedusert();
    }
}
