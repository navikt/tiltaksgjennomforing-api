package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(of = "verdi")
public class Identifikator implements Comparable<Identifikator> {
    public static final Identifikator ARENA = new Identifikator("Arena");
    public static final Identifikator SYSTEM = new Identifikator("System");

    private final String verdi;

    public Identifikator(String verdi) {
        this.verdi = verdi;
    }

    @JsonValue
    public String asString() {
        return verdi;
    }

    @Override
    public int compareTo(@NotNull Identifikator o) {
        if (this.verdi == null && o.verdi == null) {
            return 0;
        }
        if (this.verdi == null) {
            return -1;
        }
        if (o.verdi == null) {
            return 1;
        }
        return this.verdi.compareTo(o.verdi);
    }
}
