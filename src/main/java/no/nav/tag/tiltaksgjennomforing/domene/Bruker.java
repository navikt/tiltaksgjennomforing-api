package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Bruker extends Person<Fnr> {
    public Bruker(Fnr fnr) {
        super(fnr);
    }

    public Bruker(String fnr) {
        this(new Fnr(fnr));
    }

    public Fnr getFnr() {
        return getIdentifikator();
    }
}
