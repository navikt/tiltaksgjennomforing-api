package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public sealed interface ArenaMigrationProcessResult {
    record Ignored() implements ArenaMigrationProcessResult {}
    record Failed(ArenaMigrationAction action) implements ArenaMigrationProcessResult {}
    record Completed(ArenaMigrationAction action, Avtale avtale) implements ArenaMigrationProcessResult {}
}
