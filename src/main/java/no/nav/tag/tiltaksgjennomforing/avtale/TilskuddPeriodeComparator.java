package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.Comparator;

public class TilskuddPeriodeComparator implements Comparator<TilskuddPeriode> {
    @Override
    public int compare(TilskuddPeriode o1, TilskuddPeriode o2) {
        return o1.getStartDato().compareTo(o2.getStartDato());
    }
}
