package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArenaAgreementMigration {
    @Id
    private UUID id;
    private Integer tiltakgjennomforingId;
    private Integer tiltakdeltakerId;
    @Enumerated(EnumType.STRING)
    private ArenaAgreementMigrationStatus status;
    @Enumerated(EnumType.STRING)
    private ArenaMigrationAction action;
    private UUID avtaleId;
    private UUID eksternId;
    @Transient
    private LocalDateTime created;
    private LocalDateTime modified;
    @Enumerated(EnumType.STRING)
    @Convert(converter = ArenaTiltakskode.Convert.class)
    private ArenaTiltakskode tiltakstype;
    private String error;
}
