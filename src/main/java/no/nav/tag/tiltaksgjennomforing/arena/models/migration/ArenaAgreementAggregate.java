package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import com.google.common.base.Strings;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
class ArenaAgreementAggregateId {
    private Integer sakId;
    private Integer tiltakgjennomforingId;
    private Integer tiltakdeltakerId;
    private Integer personId;
    private Integer arbgivIdArrangor;
}

@Slf4j
@Entity
@IdClass(ArenaAgreementAggregateId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArenaAgreementAggregate {
    @Id
    private Integer sakId;
    @Id
    private Integer tiltakgjennomforingId;
    @Id
    private Integer tiltakdeltakerId;
    @Id
    private Integer personId;
    @Id
    private Integer arbgivIdArrangor;

    private String virksomhetsnummer;
    private String fnr;
    private String antallDagerPrUke;
    private LocalDateTime tiltakStartdato;
    private LocalDateTime tiltakSluttdato;
    private String eksternId;
    private Integer prosentDeltid;
    private Tiltakstatuskode tiltakstatuskode;
    private Deltakerstatuskode deltakerstatuskode;
    private LocalDateTime deltakerStartdato;
    private LocalDateTime deltakerSluttdato;
    private LocalDateTime regDato;

    public Optional<LocalDate> findStartdato() {
        return Stream.of(deltakerStartdato, tiltakStartdato)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .findFirst();
    }

    public Optional<LocalDate> findSluttdato() {
        return Stream.of(deltakerSluttdato, tiltakSluttdato)
            .filter(Objects::nonNull)
            .map(LocalDateTime::toLocalDate)
            .findFirst();
    }

    public Optional<UUID> getEksternIdAsUuid() throws IllegalArgumentException {
        try {
            return Strings.isNullOrEmpty(eksternId) ?  Optional.empty() : Optional.of(UUID.fromString(eksternId));
        } catch (IllegalArgumentException e) {
            log.error("Arena-avtale har ugyldig UUID som ekstern id: {}. Fortsetter uten.", eksternId);
            return Optional.empty();
        }
    }
}
