package no.nav.tag.tiltaksgjennomforing.arena.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record TiltakdeltakerEndretDto(
    @JsonProperty("TILTAKDELTAKER_ID")
    Integer tiltakdeltakerId,

    @JsonProperty("PERSON_ID")
    Integer personId,

    @JsonProperty("TILTAKGJENNOMFORING_ID")
    Integer tiltakgjennomforingId,

    @JsonProperty("DELTAKERSTATUSKODE")
    Deltakerstatuskode deltakerstatuskode,

    @JsonProperty("DELTAKERTYPEKODE")
    String deltakertypekode,

    @JsonProperty("AARSAKVERDIKODE_STATUS")
    String aarsakverdikodeStatus,

    @JsonProperty("OPPMOTETYPEKODE")
    String oppmotetypekode,

    @JsonProperty("PRIORITET")
    String prioritet,

    @JsonProperty("BEGRUNNELSE_INNSOKT")
    String begrunnelseInnsokt,

    @JsonProperty("BEGRUNNELSE_PRIORITERING")
    String begrunnelsePrioritering,

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

    @JsonProperty("DATO_SVARFRIST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoSvarfrist,

    @JsonProperty("DATO_FRA")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoFra,

    @JsonProperty("DATO_TIL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoTil,

    @JsonProperty("BEGRUNNELSE_STATUS")
    String begrunnelseStatus,

    @JsonProperty("PROSENT_DELTID")
    Integer prosentDeltid,

    @JsonProperty("BRUKERID_STATUSENDRING")
    String brukeridStatusendring,

    @JsonProperty("DATO_STATUSENDRING")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoStatusendring,

    @JsonProperty("AKTIVITET_ID")
    Integer aktivitetId,

    @JsonProperty("BRUKERID_ENDRING_PRIORITERING")
    String brukeridEndringPrioritering,

    @JsonProperty("DATO_ENDRING_PRIORITERING")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoEndringPrioritering,

    @JsonProperty("DOKUMENTKODE_SISTE_BREV")
    String dokumentkodeSisteBrev,

    @JsonProperty("STATUS_INNSOK_PAKKE")
    String statusInnsokPakke,

    @JsonProperty("STATUS_OPPTAK_PAKKE")
    String statusOpptakPakke,

    @JsonProperty("OPPLYSNINGER_INNSOK")
    String opplysningerInnsok,

    @JsonProperty("PARTISJON")
    String partisjon,

    @JsonProperty("BEGRUNNELSE_BESTILLING")
    String begrunnelseBestilling,

    @JsonProperty("ANTALL_DAGER_PR_UKE")
    String antallDagerPrUke
) {}
