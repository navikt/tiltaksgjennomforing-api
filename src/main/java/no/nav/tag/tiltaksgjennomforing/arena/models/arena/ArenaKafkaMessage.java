package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record ArenaKafkaMessage(
    String table,

    @JsonProperty("op_type")
    String opType,

    @JsonProperty("op_ts")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSS]")
    LocalDateTime opTimestamp,

    @JsonProperty("current_ts")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSS]")
    LocalDateTime currentTimestamp,

    @JsonProperty("pos")
    String pos,

    JsonNode before,
    JsonNode after
){}
