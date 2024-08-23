package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;

import java.time.LocalDate;

public class VarigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    private final int GRENSE_68_PROSENT_ETTER_12_MND = 68;
    private final int MAX_67_PROSENT_ETTER_12_MND = 67;

    public void reberegnTotal(Avtale avtale) {
        super.reberegnTotal(avtale);
       regnUtDatoOgSumRedusert(avtale);
    }

    public void regnUtDatoOgSumRedusert(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        if(avtaleInnhold.getLonnstilskuddProsent() == null || avtaleInnhold.getLonnstilskuddProsent() < GRENSE_68_PROSENT_ETTER_12_MND) {
            avtaleInnhold.setDatoForRedusertProsent(null);
            avtaleInnhold.setSumLønnstilskuddRedusert(null);
            return;
        }
        LocalDate datoForRedusertProsent = getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = regnUtRedusertLønnstilskudd(avtale);
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);

    }

    public Integer regnUtRedusertLønnstilskudd(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        if (avtaleInnhold.getDatoForRedusertProsent() != null && avtaleInnhold.getLonnstilskuddProsent() != null) {
            int lonnstilskuddProsent = avtaleInnhold.getLonnstilskuddProsent();
            if(lonnstilskuddProsent >= GRENSE_68_PROSENT_ETTER_12_MND) lonnstilskuddProsent = MAX_67_PROSENT_ETTER_12_MND;
            return getSumLonnsTilskudd(avtaleInnhold.getSumLonnsutgifter(), lonnstilskuddProsent);
        } else {
            return null;
        }
    }

}
