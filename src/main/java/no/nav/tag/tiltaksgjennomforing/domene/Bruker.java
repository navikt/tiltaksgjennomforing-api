package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.TiltaksgjennomforingException;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Bruker extends Person {
    private Fnr fnr;

    public Bruker(String fnr) {
        this(new Fnr(fnr));
    }
}
