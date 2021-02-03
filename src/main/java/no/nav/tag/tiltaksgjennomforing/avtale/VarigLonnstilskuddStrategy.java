package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

public class VarigLonnstilskuddStrategy extends LonnstilskuddStrategy {

    public VarigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        if (endreAvtale.getLonnstilskuddProsent() != null && (
            endreAvtale.getLonnstilskuddProsent() < 0 || endreAvtale.getLonnstilskuddProsent() > 75)) {
            throw new FeilLonnstilskuddsprosentException();
        }
        super.endre(endreAvtale);
    }

}
