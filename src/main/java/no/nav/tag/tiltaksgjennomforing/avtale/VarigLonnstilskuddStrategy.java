package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.util.Map;

public class VarigLonnstilskuddStrategy extends LonnstilskuddStrategy {

    public static final int MAX_75_PROSENT = 75;
    public static final int GRENSE_ETTER_12_MND_68_PROSENT = 68;
    public static final int MAX_SATS_67_PROSENT_ETTER_12_MND = 67;

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddProsent, avtaleInnhold.getLonnstilskuddProsent());
        return felterSomMåFyllesUt;
    }

    public VarigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        if (endreAvtale.getLonnstilskuddProsent() != null && (
            endreAvtale.getLonnstilskuddProsent() < 0 || endreAvtale.getLonnstilskuddProsent() > MAX_75_PROSENT)) {
            throw new FeilLonnstilskuddsprosentException();
        }
        avtaleInnhold.setLonnstilskuddProsent(endreAvtale.getLonnstilskuddProsent());
        super.endre(endreAvtale);
    }

    @Override
    public void regnUtTotalLonnstilskudd() {
        super.regnUtTotalLonnstilskudd();
        regnutEtter12MånederVarighetOgOver68ProsentTilskudd();
    }
    private LocalDate getDatoForRedusertProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddprosent) {
        if (startDato == null || sluttDato == null || lonnstilskuddprosent == null) {
            return null;
        }
        if (startDato.plusYears(1).minusDays(1).isBefore(sluttDato)) {
            return startDato.plusYears(1);
        }
        return null;
    }
    private void regnutEtter12MånederVarighetOgOver68ProsentTilskudd() {
        if(avtaleInnhold.getLonnstilskuddProsent() == null || avtaleInnhold.getLonnstilskuddProsent() < GRENSE_ETTER_12_MND_68_PROSENT) return;
        LocalDate datoForRedusertProsent = getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        if(datoForRedusertProsent != null){
            avtaleInnhold.setLonnstilskuddProsent(MAX_SATS_67_PROSENT_ETTER_12_MND);
            avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
            Integer sumLønnstilskuddRedusert = regnUtRedusertLønnstilskudd();
            avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
        }
    }

    private Integer regnUtRedusertLønnstilskudd() {
        if (avtaleInnhold.getDatoForRedusertProsent() != null && avtaleInnhold.getLonnstilskuddProsent() != null) {
            return getSumLonnsTilskudd(avtaleInnhold.getSumLonnsutgifter(), avtaleInnhold.getLonnstilskuddProsent());
        } else {
            return null;
        }
    }

}
