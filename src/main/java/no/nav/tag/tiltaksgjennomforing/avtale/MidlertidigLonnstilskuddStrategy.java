package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStrategy extends LonnstilskuddStrategy {
    Kvalifiseringsgruppe kvalifiseringsgruppe;

    public MidlertidigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold, Kvalifiseringsgruppe kvalifiseringsgruppe) {
        super(avtaleInnhold);
        this.kvalifiseringsgruppe = kvalifiseringsgruppe;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        if(kvalifiseringsgruppe != null) {
            final EndreAvtale endreAvtaleMedOppdatertProsentsats = settTilskuddsprosentSats(endreAvtale);
            sjekktilskuddsprosentSats(endreAvtaleMedOppdatertProsentsats);
            super.endre(endreAvtaleMedOppdatertProsentsats);
        }else {
            sjekktilskuddsprosentSats(endreAvtale);
            super.endre(endreAvtale);
        }
    }

    @Override
    public void regnUtTotalLonnstilskudd() {
        super.regnUtTotalLonnstilskudd();
        regnUtDatoOgSumRedusert();
    }

    private void sjekktilskuddsprosentSats(EndreAvtale endreAvtale) {
        if (endreAvtale.getLonnstilskuddProsent() != null && (
                endreAvtale.getLonnstilskuddProsent() != 40 && endreAvtale.getLonnstilskuddProsent() != 60)) {
            throw new FeilLonnstilskuddsprosentException();
        }
    }

    private EndreAvtale settTilskuddsprosentSats(EndreAvtale endreAvtale) {
        final Integer sats = kvalifiseringsgruppe.finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60);
        endreAvtale.setLonnstilskuddProsent(sats);
        return endreAvtale;
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
