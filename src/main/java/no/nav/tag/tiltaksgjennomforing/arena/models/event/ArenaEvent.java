package no.nav.tag.tiltaksgjennomforing.arena.models.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaPos;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTable;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Optional;
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
    private String pos;
    private LocalDateTime currentTs;

    public static ArenaEvent create(
        String id,
        String arenaTable,
        String operation,
        String pos,
        LocalDateTime currentTs,
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
            .currentTs(currentTs)
            .payload(payload)
            .created(Now.localDateTime())
            .retryCount(0)
            .status(status)
            .pos(pos)
            .build();
    }

    public ArenaPos getArenaPos() {
        return ArenaPos.parse(this.pos);
    }

    public ArenaTable getArenaTable() {
        return ArenaTable.parse(this.arenaTable);
    }

    public Operation getOperation() {
        return Operation.parse(this.operation);
    }

    public Optional<String> getPayloadFieldAsText(String fieldName) {
        return Optional.ofNullable(this.payload.get(fieldName))
            .flatMap(node ->
                node instanceof NullNode || !node.isValueNode() ? Optional.empty() : Optional.of(node.asText())
            );
    }

    public String getLogId() {
        return this.getArenaTable() + ":" + this.getArenaId();
    }
}
