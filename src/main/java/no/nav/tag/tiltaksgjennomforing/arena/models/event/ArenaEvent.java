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

import java.time.LocalDateTime;
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
    private LocalDateTime created;
    @Enumerated(EnumType.STRING)
    private ArenaEventStatus status;
    private int retryCount;
    private String operation;
    private LocalDateTime operationTime;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    public static ArenaEvent create(
        String id,
        String arenaTable,
        String operation,
        LocalDateTime operationTime,
        JsonNode payload
    ) {
        return create(id, arenaTable, operation, operationTime, payload, ArenaEventStatus.PENDING);
    }

    public static ArenaEvent create(
        String id,
        String arenaTable,
        String operation,
        LocalDateTime operationTime,
        JsonNode payload,
        ArenaEventStatus status
    ) {
        return ArenaEvent.builder()
            .arenaId(id)
            .arenaTable(arenaTable)
            .id(UUID.randomUUID())
            .operation(operation)
            .operationTime(operationTime)
            .payload(payload)
            .created(LocalDateTime.now())
            .retryCount(0)
            .status(status)
            .build();
    }
}
