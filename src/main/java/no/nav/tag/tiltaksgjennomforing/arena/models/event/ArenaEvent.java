package no.nav.tag.tiltaksgjennomforing.arena.models.event;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArenaEvent {
    @Id
    private UUID id;
    private String arenaId;
    private String arenaTable;
    @Enumerated(EnumType.STRING)
    private ArenaEventStatus status;
    private int retryCount;
    private String operation;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    public static ArenaEvent create(
        String id,
        String arenaTable,
        String operation,
        JsonNode payload
    ) {
        return create(id, arenaTable, operation, payload, ArenaEventStatus.PENDING);
    }

    public static ArenaEvent create(
        String id,
        String arenaTable,
        String operation,
        JsonNode payload,
        ArenaEventStatus status
    ) {
        return ArenaEvent.builder()
            .id(UUID.randomUUID())
            .arenaId(id)
            .arenaTable(arenaTable)
            .operation(operation)
            .status(status)
            .retryCount(0)
            .payload(payload)
            .build();
    }
}