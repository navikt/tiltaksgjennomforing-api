package no.nav.tag.tiltaksgjennomforing.arena.client;

import java.util.List;

public record ArenaOrdsFnrResponse(
    List<Person> personListe
) {
    public record Person(
        int personId,
        String fnr
    ) {
    }
}
