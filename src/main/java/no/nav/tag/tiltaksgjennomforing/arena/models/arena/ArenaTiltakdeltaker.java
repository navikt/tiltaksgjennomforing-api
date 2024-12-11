package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArenaTiltakdeltaker {
    @Id
    @JsonProperty("TILTAKDELTAKER_ID")
    private Integer tiltakdeltakerId;

    @JsonProperty("PERSON_ID")
    private Integer personId;

    @JsonProperty("TILTAKGJENNOMFORING_ID")
    private Integer tiltakgjennomforingId;

    @Enumerated(EnumType.STRING)
    @JsonProperty("DELTAKERSTATUSKODE")
    private Deltakerstatuskode deltakerstatuskode;

    @JsonProperty("DELTAKERTYPEKODE")
    private String deltakertypekode;

    @JsonProperty("AARSAKVERDIKODE_STATUS")
    private String aarsakverdikodeStatus;

    @JsonProperty("OPPMOTETYPEKODE")
    private String oppmotetypekode;

    @JsonProperty("PRIORITET")
    private String prioritet;

    @JsonProperty("BEGRUNNELSE_INNSOKT")
    private String begrunnelseInnsokt;

    @JsonProperty("BEGRUNNELSE_PRIORITERING")
    private String begrunnelsePrioritering;

    @JsonProperty("REG_DATO")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime regDato;

    @JsonProperty("REG_USER")
    private String regUser;

    @JsonProperty("MOD_DATO")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modDato;

    @JsonProperty("MOD_USER")
    private String modUser;

    @JsonProperty("DATO_SVARFRIST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoSvarfrist;

    @JsonProperty("DATO_FRA")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoFra;

    @JsonProperty("DATO_TIL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoTil;

    @JsonProperty("BEGRUNNELSE_STATUS")
    private String begrunnelseStatus;

    @JsonProperty("PROSENT_DELTID")
    private String prosentDeltid;

    @JsonProperty("BRUKERID_STATUSENDRING")
    private String brukeridStatusendring;

    @JsonProperty("DATO_STATUSENDRING")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoStatusendring;

    @JsonProperty("AKTIVITET_ID")
    private Integer aktivitetId;

    @JsonProperty("BRUKERID_ENDRING_PRIORITERING")
    private String brukeridEndringPrioritering;

    @JsonProperty("DATO_ENDRING_PRIORITERING")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoEndringPrioritering;

    @JsonProperty("DOKUMENTKODE_SISTE_BREV")
    private String dokumentkodeSisteBrev;

    @JsonProperty("STATUS_INNSOK_PAKKE")
    private String statusInnsokPakke;

    @JsonProperty("STATUS_OPPTAK_PAKKE")
    private String statusOpptakPakke;

    @JsonProperty("OPPLYSNINGER_INNSOK")
    private String opplysningerInnsok;

    @JsonProperty("PARTISJON")
    private String partisjon;

    @JsonProperty("BEGRUNNELSE_BESTILLING")
    private String begrunnelseBestilling;

    @JsonProperty("ANTALL_DAGER_PR_UKE")
    private String antallDagerPrUke;

    @JsonProperty("EKSTERN_ID")
    private String eksternId;
}
