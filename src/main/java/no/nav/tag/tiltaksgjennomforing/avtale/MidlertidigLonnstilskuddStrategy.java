package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMidlertidigLonnstilskuddException;

public class MidlertidigLonnstilskuddStrategy extends LonnstilskuddStrategy {

    public MidlertidigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        if (endreAvtale.getLonnstilskuddProsent() != null && (
            endreAvtale.getLonnstilskuddProsent() != 40 && endreAvtale.getLonnstilskuddProsent() != 60)) {
            throw new FeilLonnstilskuddsprosentException();
        }
        super.endre(endreAvtale);
    }

    @Override
    public void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.plusMonths(24).isBefore(sluttDato)) {
            throw new VarighetForLangMidlertidigLonnstilskuddException();
        }
    }

    @Override
    void regnUtTotalLonnstilskudd() {

        super.regnUtTotalLonnstilskudd();

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

}
