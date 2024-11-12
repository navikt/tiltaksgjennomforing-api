package no.nav.tag.tiltaksgjennomforing.arena.client.ords;

import java.util.List;

public record ArenaOrdsFnrResponse(
    List<Person> personListe
) {
    public record Person(
        Integer personId,
        String fnr
    ) {
    }
}
