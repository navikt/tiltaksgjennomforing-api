package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;

public class Arbeidsgiver extends Avtalepart<Fnr> {
    public Arbeidsgiver(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForArbeidsgiver(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    boolean kanOppheveGodkjenninger() {
        return !avtale.erGodkjentAvVeileder();
    }

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.ARBEIDSGIVER;
    }

    @Override
    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn) {
        throw new TilgangskontrollException("Arbeidsgiver kan ikke godkjenne som veileder");
    }
}
