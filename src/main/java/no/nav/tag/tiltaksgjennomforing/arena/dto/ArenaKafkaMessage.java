package no.nav.tag.tiltaksgjennomforing.arena.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ArenaKafkaMessage<T>(
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

    T before,
    T after
){}
