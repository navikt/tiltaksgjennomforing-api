package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

import java.time.LocalDate;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;

public class BaseAvtaleInnholdStrategy implements AvtaleInnholdStrategy {
    final AvtaleInnhold avtaleInnhold;

    public BaseAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        this.avtaleInnhold = avtaleInnhold;
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        this.sjekkAtStartDatoErEtterSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
        this.sjekkOmVarighetErForLang(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
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

    protected void sjekkAtStartDatoErEtterSluttDato(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null) {
            if (startDato.isAfter(sluttDato)) {
                throw new StartDatoErEtterSluttDatoException();
            }
        }
    }
}
