package no.nav.tag.tiltaksgjennomforing.arena.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.nav.tag.tiltaksgjennomforing.arena.utils.ArenaBooleanDeserializer;

import java.time.LocalDateTime;

public record TiltaksakEndretDto(

    @JsonProperty("SAK_ID")
    Integer sakId,

    @JsonProperty("SAKSKODE")
    String sakskode,

    @JsonProperty("REG_DATO")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime regDato,

    @JsonProperty("REG_USER")
    String regUser,

    @JsonProperty("MOD_DATO")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime modDato,

    @JsonProperty("MOD_USER")
    String modUser,

    @JsonProperty("TABELLNAVNALIAS")
    String tabellnavnalias,

    @JsonProperty("OBJEKT_ID")
    Integer objektId,

    @JsonProperty("AAR")
    Integer aar,

    @JsonProperty("LOPENRSAK")
    Integer lopenrsak,

    @JsonProperty("DATO_AVSLUTTET")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoAvsluttet,

    @JsonProperty("SAKSTATUSKODE")
    String sakstatuskode,

    @JsonProperty("ARKIVNOKKEL")
    String arkivnokkel,

    @JsonProperty("AETATENHET_ARKIV")
    String aetatenhetArkiv,

    @JsonProperty("ARKIVHENVISNING")
    String arkivhenvisning,

    @JsonProperty("BRUKERID_ANSVARLIG")
    String brukeridAnsvarlig,

    @JsonProperty("AETATENHET_ANSVARLIG")
    String aetatenhetAnsvarlig,

    @JsonProperty("OBJEKT_KODE")
    String objektKode,

    @JsonProperty("STATUS_ENDRET")
    String statusEndret,

    @JsonProperty("PARTISJON")
    String partisjon,

    @JsonProperty("ER_UTLAND")
    @JsonDeserialize(using = ArenaBooleanDeserializer.class)
    Boolean erUtland
) {}
