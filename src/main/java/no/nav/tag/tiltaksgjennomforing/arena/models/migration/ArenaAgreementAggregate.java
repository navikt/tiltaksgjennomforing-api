package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import com.google.common.base.Strings;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@EqualsAndHashCode
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
    private String eksternIdTiltak;
    private String eksternIdDeltaker;
    private String prosentDeltid;
    private Tiltakstatuskode tiltakstatuskode;
    private Deltakerstatuskode deltakerstatuskode;
    private LocalDateTime deltakerStartdato;
    private LocalDateTime deltakerSluttdato;
    private LocalDateTime regDato;
    @Convert(converter = ArenaTiltakskode.Convert.class)
    private ArenaTiltakskode tiltakskode;

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

    public boolean isDublett() {
        return isDublett(eksternIdTiltak) || isDublett(eksternIdDeltaker);
    }

    public Optional<UUID> getEksternIdAsUuid() throws IllegalArgumentException {
        Optional<String> eksternId = Optional.ofNullable(eksternIdDeltaker)
            .filter(id -> !Strings.isNullOrEmpty(id) && !isDublett(id));

        if (eksternId.isEmpty() && tiltakdeltakerId == null) {
            eksternId = Optional.ofNullable(eksternIdTiltak)
                .filter(id -> !Strings.isNullOrEmpty(id) && !isDublett(id));
        }

        try {
            return eksternId.map(UUID::fromString);
        } catch (IllegalArgumentException e) {
            log.error("Arena-avtale har ugyldig UUID som ekstern id: {}. Fortsetter uten.", eksternId.orElse(null));
            return Optional.empty();
        }
    }

    private static boolean isDublett(String eksternId) {
        return Optional.ofNullable(eksternId)
            .map(id -> id.toLowerCase().contains("dublett"))
            .orElse(false);
    }
}
