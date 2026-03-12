package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VarigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    public static final int TILSKUDDSPROSENT_MAKS = 75;
    public static final int TILSKUDDSPROSENT_REDUSERT_MAKS = 67;

    @Override
    public Integer getProsentForPeriode(Avtale avtale, Periode tilskuddsperiode) {
        Optional<LocalDate> datoForRedusertProsent = getDatoerForReduksjon(avtale).stream().findFirst();

        boolean erRedusert = datoForRedusertProsent
            .map(dato -> !tilskuddsperiode.getSlutt().isBefore(dato))
            .orElse(false);

        int lonnstilskuddProsent = avtale.getGjeldendeInnhold().getLonnstilskuddProsent() != null ? avtale.getGjeldendeInnhold().getLonnstilskuddProsent() : 0;

        return Math.min(lonnstilskuddProsent, erRedusert ? TILSKUDDSPROSENT_REDUSERT_MAKS : TILSKUDDSPROSENT_MAKS);
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(Avtale avtale) {
        LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate sluttDato = avtale.getGjeldendeInnhold().getSluttDato();
        if (startDato == null || sluttDato == null) {
            return Collections.emptyList();
        }

        Integer lonnstilskuddProsent = avtale.getGjeldendeInnhold().getLonnstilskuddProsent();
        if (lonnstilskuddProsent != null && lonnstilskuddProsent <= TILSKUDDSPROSENT_REDUSERT_MAKS) {
            return Collections.emptyList();
        }

        LocalDate datoForReduksjon = avtale.getGjeldendeInnhold().getStartDato().plusYears(1);
        if (datoForReduksjon.isAfter(sluttDato)) {
            return Collections.emptyList();
        }

        return List.of(datoForReduksjon);
    }

    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setLonnstilskuddProsent(endreTilskuddsberegning.getLonnstilskuddProsent());
        super.endreBeregning(avtale, endreTilskuddsberegning);
    }

    @Override
    public void reberegnTotal(Avtale avtale) {
        super.reberegnTotal(avtale);
       regnUtDatoOgSumRedusert(avtale);
    }

    public void regnUtDatoOgSumRedusert(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        if (avtaleInnhold.getLonnstilskuddProsent() == null || avtaleInnhold.getLonnstilskuddProsent() <= TILSKUDDSPROSENT_REDUSERT_MAKS) {
            avtaleInnhold.setDatoForRedusertProsent(null);
            avtaleInnhold.setSumLønnstilskuddRedusert(null);
            return;
        }
        LocalDate datoForRedusertProsent = getDatoerForReduksjon(avtale).stream().findFirst().orElse(null);
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = regnUtRedusertLønnstilskudd(avtale);
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }

    public Integer regnUtRedusertLønnstilskudd(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        if (avtaleInnhold.getDatoForRedusertProsent() != null && avtaleInnhold.getLonnstilskuddProsent() != null) {
            int lonnstilskuddProsent = avtaleInnhold.getLonnstilskuddProsent();
            if (lonnstilskuddProsent > TILSKUDDSPROSENT_REDUSERT_MAKS) {
                lonnstilskuddProsent = TILSKUDDSPROSENT_REDUSERT_MAKS;
            }
            return getSumLonnstilskudd(avtaleInnhold.getSumLonnsutgifter(), lonnstilskuddProsent);
        } else {
            return null;
        }
    }

}
