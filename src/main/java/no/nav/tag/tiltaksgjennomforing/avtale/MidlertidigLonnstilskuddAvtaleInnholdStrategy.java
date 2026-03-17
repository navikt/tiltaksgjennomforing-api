package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_TILPASSET;

public class MidlertidigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<MidlertidigLonnstilskuddAvtaleBeregningStrategy> {
    public MidlertidigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new MidlertidigLonnstilskuddAvtaleBeregningStrategy());
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
        Integer lonnstilskuddProsent = endreAvtale.getLonnstilskuddProsent();
        if (lonnstilskuddProsent == null) {
            return;
        }
        if (lonnstilskuddProsent != MidlertidigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT && lonnstilskuddProsent != TILSKUDDSPROSENT_TILPASSET) {
            throw new FeilLonnstilskuddsprosentException();
        }
    }

    private void settTilskuddsprosentSats(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        final Integer sats = kvalifiseringsgruppe.finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60);
        avtaleInnhold.setLonnstilskuddProsent(sats);
    }

    private void regnUtDatoOgSumRedusert() {
        Avtale avtale = avtaleInnhold.getAvtale();
        LocalDate datoForRedusertProsent = beregningStrategy.getDatoerForReduksjon(avtale).stream().findFirst().orElse(null);
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = beregningStrategy.regnUtRedusertLønnstilskudd(avtale);
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }

    @Override
    public void endreSluttDato(LocalDate nySluttDato) {
        super.endreSluttDato(nySluttDato);
        regnUtDatoOgSumRedusert();
    }
}
