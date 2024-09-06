package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public sealed interface ArenaMigrationProcessResult {
    record Ignored() implements ArenaMigrationProcessResult {}
    record Completed(ArenaAgreementMigrationStatus status, Avtale avtale) implements ArenaMigrationProcessResult {}
}
