package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

public interface ArenaAgreementMigrationCount {
    ArenaAgreementMigrationStatus status();
    ArenaMigrationAction action();
    long count();
}
