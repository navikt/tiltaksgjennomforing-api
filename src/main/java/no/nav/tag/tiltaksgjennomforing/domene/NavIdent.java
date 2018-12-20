package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.TiltaksgjennomforingException;

@Value
public class NavIdent {
    private final String id;

    public NavIdent(String id) {
        if (!erGyldigNavIdent(id)) {
            throw new TiltaksgjennomforingException("Ugyldig format på NAV-ident.");
        }
        this.id = id;
    }

    private static boolean erGyldigNavIdent(String id) {
        return true;
    }

    @JsonValue
    public String getId() {
        return id;
    }
}
