package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.time.LocalDate;

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
    void regnUtTotalLonnstilskudd() {
        super.regnUtTotalLonnstilskudd();
        regnUtDatoOgSumRedusert();
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
