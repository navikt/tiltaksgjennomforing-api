package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

public class SommerjobbStrategy extends LonnstilskuddStrategy {

    public SommerjobbStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Integer lonnstilskuddProsent = endreAvtale.getLonnstilskuddProsent();
        if (lonnstilskuddProsent != null && !(lonnstilskuddProsent == 75 || lonnstilskuddProsent == 50)) {
            throw new FeilLonnstilskuddsprosentException();
        }
        endreAvtale.setStillingstype(Stillingstype.MIDLERTIDIG);
        super.endre(endreAvtale);
    }

}
