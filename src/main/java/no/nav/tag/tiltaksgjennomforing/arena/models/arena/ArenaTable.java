package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ArenaTable {
    TILTAKDELTAKER("SIAMO.TILTAKDELTAKER"),
    TILTAKGJENNOMFORING("SIAMO.TILTAKGJENNOMFORING");

    private final String table;

    public static ArenaTable parse(String table) {
        return Arrays.stream(ArenaTable.values())
            .filter(value -> value.getTable().equals(table))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ugyldig tabell: " + table));
    }
}
