package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.arena.utils.ArenaBooleanDeserializer;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ArenaTiltakssak {
    private static final String SAKSKODE_TILTAK = "TILT";

    @Id
    @JsonProperty("SAK_ID")
    private Integer sakId;

    @JsonProperty("SAKSKODE")
    private String sakskode;

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

    @JsonProperty("TABELLNAVNALIAS")
    private String tabellnavnalias;

    @JsonProperty("OBJEKT_ID")
    private Integer objektId;

    @JsonProperty("AAR")
    private Integer aar;

    @JsonProperty("LOPENRSAK")
    private Integer lopenrsak;

    @JsonProperty("DATO_AVSLUTTET")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoAvsluttet;

    @JsonProperty("SAKSTATUSKODE")
    private String sakstatuskode;

    @JsonProperty("ARKIVNOKKEL")
    private String arkivnokkel;

    @JsonProperty("AETATENHET_ARKIV")
    private String aetatenhetArkiv;

    @JsonProperty("ARKIVHENVISNING")
    private String arkivhenvisning;

    @JsonProperty("BRUKERID_ANSVARLIG")
    private String brukeridAnsvarlig;

    @JsonProperty("AETATENHET_ANSVARLIG")
    private String aetatenhetAnsvarlig;

    @JsonProperty("OBJEKT_KODE")
    private String objektKode;

    @JsonProperty("STATUS_ENDRET")
    private String statusEndret;

    @JsonProperty("PARTISJON")
    private String partisjon;

    @JsonProperty("ER_UTLAND")
    @JsonDeserialize(using = ArenaBooleanDeserializer.class)
    private Boolean erUtland;

    public boolean isTiltaksgjennomforing() {
        return SAKSKODE_TILTAK.equals(this.getSakskode());
    }

    public boolean hasEnhent() {
        return !Strings.isNullOrEmpty(this.getAetatenhetAnsvarlig());
    }
}
