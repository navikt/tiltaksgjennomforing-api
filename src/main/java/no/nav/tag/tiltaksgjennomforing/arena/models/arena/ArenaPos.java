package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class ArenaPos implements Comparable<ArenaPos> {
    private final BigInteger value;

    public static ArenaPos parse(String value) {
        return new ArenaPos(
            Optional.ofNullable(value)
                .map(p -> new BigInteger(p.replaceAll("^0+", "")))
                .orElse(null)
        );
    }

    @Override
    public int compareTo(@NotNull ArenaPos o) {
        if (this.value == null && o.value == null) {
            return 0;
        }
        if (this.value == null) {
            return -1;
        }
        if (o.value == null) {
            return 1;
        }
        return this.value.compareTo(o.value);
    }
}
