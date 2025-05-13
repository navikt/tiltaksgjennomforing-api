package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
class ArenaAgreementMigrationCountId {
    private ArenaAgreementMigrationStatus status;
    private ArenaMigrationAction action;
}

@Entity
@IdClass(ArenaAgreementMigrationCountId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArenaAgreementMigrationCount {
    @Id
    private ArenaAgreementMigrationStatus status;
    @Id
    private ArenaMigrationAction action;
    private long count;
}
