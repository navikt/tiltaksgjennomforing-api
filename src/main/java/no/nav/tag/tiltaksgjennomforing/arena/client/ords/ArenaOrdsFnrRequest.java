package no.nav.tag.tiltaksgjennomforing.arena.client.ords;

import java.util.List;

public record ArenaOrdsFnrRequest(List<Person> personListe) {
    public record Person(Integer personId) {}
}
