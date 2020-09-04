package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.AvtalensVarighetMerEnnMaksimaltAntallMånederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class BaseAvtaleInnholdStrategy implements AvtaleInnholdStrategy {
    final AvtaleInnhold avtaleInnhold;

    public BaseAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        this.avtaleInnhold = avtaleInnhold;
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        this.sjekkStartogSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
        avtaleInnhold.setDeltakerFornavn(nyAvtale.getDeltakerFornavn());
        avtaleInnhold.setDeltakerEtternavn(nyAvtale.getDeltakerEtternavn());
        avtaleInnhold.setDeltakerTlf(nyAvtale.getDeltakerTlf());
        avtaleInnhold.setBedriftNavn(nyAvtale.getBedriftNavn());
        avtaleInnhold.setArbeidsgiverFornavn(nyAvtale.getArbeidsgiverFornavn());
        avtaleInnhold.setArbeidsgiverEtternavn(nyAvtale.getArbeidsgiverEtternavn());
        avtaleInnhold.setArbeidsgiverTlf(nyAvtale.getArbeidsgiverTlf());
        avtaleInnhold.setVeilederFornavn(nyAvtale.getVeilederFornavn());
        avtaleInnhold.setVeilederEtternavn(nyAvtale.getVeilederEtternavn());
        avtaleInnhold.setVeilederTlf(nyAvtale.getVeilederTlf());
        avtaleInnhold.setArbeidsoppgaver(nyAvtale.getArbeidsoppgaver());
        avtaleInnhold.setOppfolging(nyAvtale.getOppfolging());
        avtaleInnhold.setTilrettelegging(nyAvtale.getTilrettelegging());
        avtaleInnhold.setStartDato(nyAvtale.getStartDato());
        avtaleInnhold.setSluttDato(nyAvtale.getSluttDato());
        avtaleInnhold.setStillingprosent(nyAvtale.getStillingprosent());
    }

    @Override
    public boolean erAltUtfylt() {
        return erIkkeTomme(
                avtaleInnhold.getDeltakerFornavn(),
                avtaleInnhold.getDeltakerEtternavn(),
                avtaleInnhold.getDeltakerTlf(),
                avtaleInnhold.getBedriftNavn(),
                avtaleInnhold.getArbeidsgiverFornavn(),
                avtaleInnhold.getArbeidsgiverEtternavn(),
                avtaleInnhold.getArbeidsgiverTlf(),
                avtaleInnhold.getVeilederFornavn(),
                avtaleInnhold.getVeilederEtternavn(),
                avtaleInnhold.getVeilederTlf(),
                avtaleInnhold.getOppfolging(),
                avtaleInnhold.getTilrettelegging(),
                avtaleInnhold.getStartDato(),
                avtaleInnhold.getSluttDato(),
                avtaleInnhold.getStillingprosent()
        );
    }

    protected void sjekkStartogSluttDato(LocalDate startDato, LocalDate sluttDato){
        startOgSluttDatoErSattRiktig(startDato, sluttDato);
    }

    protected boolean startOgSluttDatoErSattRiktig(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null) {
            if (startDato.isAfter(sluttDato)) {
                throw new StartDatoErEtterSluttDatoException();
            }
            return true;
        }
        return false;
    }

    protected void startOgSluttDatoMedVarighetErSattRiktig(LocalDate startDato, LocalDate sluttDato, Integer varighet) {
        if(!startOgSluttDatoErSattRiktig(startDato, sluttDato)){
            return;
        }
        if (sluttDato.isAfter(startDato.plusMonths(varighet))) {
            throw new AvtalensVarighetMerEnnMaksimaltAntallMånederException(varighet);
        }
    }
}
