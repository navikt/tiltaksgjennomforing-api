package no.nav.tag.tiltaksgjennomforing.avtale;

import org.apache.commons.lang3.StringUtils;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class LonnstilskuddStrategy extends BaseAvtaleInnholdStrategy {
    public LonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setLonnstilskuddProsent(nyAvtale.getLonnstilskuddProsent());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());
        avtaleInnhold.setFeriepengesats(nyAvtale.getFeriepengesats());
        avtaleInnhold.setArbeidsgiveravgift(nyAvtale.getArbeidsgiveravgift());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        super.endre(nyAvtale);
    }

    private boolean erFamiletilknytningForklaringFylltUtHvisDetTrengs() {
        if (avtaleInnhold.getHarFamilietilknytning()) {
            return StringUtils.isNotBlank(avtaleInnhold.getFamilietilknytningForklaring());
        } else {
            return true;
        }
    }

    @Override
    public boolean erAltUtfylt() {
        return super.erAltUtfylt() && erIkkeTomme(
                avtaleInnhold.getArbeidsgiverKontonummer(),
                avtaleInnhold.getStillingstittel(),
                avtaleInnhold.getArbeidsoppgaver(),
                avtaleInnhold.getLonnstilskuddProsent(),
                avtaleInnhold.getManedslonn(),
                avtaleInnhold.getFeriepengesats(),
                avtaleInnhold.getArbeidsgiveravgift(),
                avtaleInnhold.getHarFamilietilknytning()
        ) && erFamiletilknytningForklaringFylltUtHvisDetTrengs();
    }
}
