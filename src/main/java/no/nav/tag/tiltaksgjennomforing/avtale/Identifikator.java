package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "verdi")
public class Identifikator {
    public static final Identifikator ARENA = new Identifikator("Arena");
    public static final Identifikator SYSTEM = new Identifikator("System");
    public static final Identifikator TILTAKSGJENNOMFORING_API = new Identifikator("tiltaksgjennomforing-api");

    private final String verdi;

    public Identifikator(String verdi) {
        this.verdi = verdi;
    }

    @JsonValue
    public String asString() {
        return verdi;
    }
}
