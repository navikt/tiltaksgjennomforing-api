package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class ArbeidstreningStrategy extends BaseAvtaleInnholdStrategy {

    private static final int MAKSIMALT_ANTALL_MÅNEDER_VARIGHET = 18;

    public ArbeidstreningStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        nyAvtale.getMaal().forEach(Maal::sjekkMaalLengde);
        nyAvtale.getOppgaver().forEach(Oppgave::sjekkOppgaveLengde);
        avtaleInnhold.getMaal().clear();
        avtaleInnhold.getMaal().addAll(nyAvtale.getMaal());
        avtaleInnhold.getMaal().forEach(m -> m.setAvtaleInnhold(avtaleInnhold));
        avtaleInnhold.getOppgaver().clear();
        avtaleInnhold.getOppgaver().addAll(nyAvtale.getOppgaver());
        avtaleInnhold.getOppgaver().forEach(o -> o.setAvtaleInnhold(avtaleInnhold));
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        super.endre(nyAvtale);
    }

    @Override
    public boolean erAltUtfylt() {
        // Inntil data er migrert kan arbeidsoppgaver være fylt ut som fritekst eller i arbeidsoppgaver-tabellen
        boolean arbeidsoppgaverErUtfylt = !avtaleInnhold.getOppgaver().isEmpty() || erIkkeTomme(avtaleInnhold.getArbeidsoppgaver());

        return super.erAltUtfylt()
                && !avtaleInnhold.getMaal().isEmpty()
                && arbeidsoppgaverErUtfylt
                && erIkkeTomme(avtaleInnhold.getStillingstittel());
    }

    @Override
    protected void sjekkStartogSluttDato(LocalDate startDato, LocalDate sluttDato) {
        super.startOgSluttDatoMedVarighetErSattRiktig(startDato, sluttDato, MAKSIMALT_ANTALL_MÅNEDER_VARIGHET);
    }
}
