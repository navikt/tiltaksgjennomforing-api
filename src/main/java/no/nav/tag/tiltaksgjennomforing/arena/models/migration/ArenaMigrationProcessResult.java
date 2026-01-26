package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public sealed interface ArenaMigrationProcessResult {
    enum Error {
        FNR_STEMMER_IKKE,
        KODE_6,
        MANGLER_FNR,
        MANGLER_VIRKSOMHETSNUMMER,
        VIRKSOMHETSNUMMER_STEMMER_IKKE,
    }

    record Ignored() implements ArenaMigrationProcessResult {}
    record Failed(Error error) implements ArenaMigrationProcessResult {}
    record Completed(ArenaMigrationAction action, Avtale avtale) implements ArenaMigrationProcessResult {}
}
