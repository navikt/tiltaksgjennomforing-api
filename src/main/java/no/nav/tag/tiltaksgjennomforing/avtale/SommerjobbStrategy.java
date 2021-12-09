package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

public class SommerjobbStrategy extends LonnstilskuddStrategy {
    Kvalifiseringsgruppe kvalifiseringsgruppe;

    public SommerjobbStrategy(AvtaleInnhold avtaleInnhold, Kvalifiseringsgruppe kvalifiseringsgruppe) {
        super(avtaleInnhold);
        this.kvalifiseringsgruppe = kvalifiseringsgruppe;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        endreAvtale.setStillingstype(Stillingstype.MIDLERTIDIG);
        sjekkSommerjobbProsentsats(endreAvtale);
        super.endre(endreAvtale);
        // Midlertidig skrudd av utleding av lønnstilskuddprosent fra kvalifiseringsgruppe for å åpne for etterregistrering.
//        if(kvalifiseringsgruppe != null) {
//            final EndreAvtale endreAvtaleMedOppdatertProsentsats = settTilskuddsprosentSats(endreAvtale);
//            sjekkSommerjobbProsentsats(endreAvtaleMedOppdatertProsentsats);
//            super.endre(endreAvtaleMedOppdatertProsentsats);
//        }else {
//            sjekkSommerjobbProsentsats(endreAvtale);
//            super.endre(endreAvtale);
//        }
    }

    private void sjekkSommerjobbProsentsats(EndreAvtale endreAvtale) {
        Integer lonnstilskuddProsent = endreAvtale.getLonnstilskuddProsent();
        if (lonnstilskuddProsent != null && !(lonnstilskuddProsent == 75 || lonnstilskuddProsent == 50)) {
            throw new FeilLonnstilskuddsprosentException();
        }
    }

    private EndreAvtale settTilskuddsprosentSats(EndreAvtale endreAvtale) {
        final Integer sats = kvalifiseringsgruppe.finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(50, 75);
        endreAvtale.setLonnstilskuddProsent(sats);
        return endreAvtale;
    }

}
