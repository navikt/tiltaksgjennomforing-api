package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;

public class Veileder extends Avtalepart<NavIdent> {

    public Veileder(NavIdent identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForVeileder();
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public void sjekkOmAvtaleKanGodkjennes() {
        if (!avtale.isGodkjentAvArbeidsgiver() || !avtale.isGodkjentAvDeltaker()) {
            throw new TiltaksgjennomforingException("Veileder må godkjenne avtalen etter deltaker og arbeidsgiver.");
        }
    }

    @Override
    boolean kanOppheveGodkjenninger() {
        return true;
    }

    @Override
    public Rolle rolle() {
        return Rolle.VEILEDER;
    }
}
