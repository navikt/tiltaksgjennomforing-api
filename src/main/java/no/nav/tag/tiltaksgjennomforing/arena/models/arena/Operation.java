package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Operation {
    INSERT("I"),
    UPDATE("U"),
    DELETE("D");

    private final String operation;

    public static Operation parse(String operation) {
        return Arrays.stream(Operation.values())
            .filter(value -> value.getOperation().equals(operation))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ugyldig operasjon: " + operation));
    }
}
