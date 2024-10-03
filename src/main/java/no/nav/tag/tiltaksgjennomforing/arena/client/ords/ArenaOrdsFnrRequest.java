package no.nav.tag.tiltaksgjennomforing.arena.client.ords;

import java.util.List;

public record ArenaOrdsFnrRequest(
    List<Integer> personListe
) { }
