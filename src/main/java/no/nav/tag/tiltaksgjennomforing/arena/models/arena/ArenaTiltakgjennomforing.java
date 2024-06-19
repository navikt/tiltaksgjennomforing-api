package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArenaTiltakgjennomforing {
    private static final String ARBEIDSTRENING = "ARBTREN";

    @Id
    @JsonProperty("TILTAKGJENNOMFORING_ID")
    private Integer tiltakgjennomforingId;

    @JsonProperty("SAK_ID")
    private Integer sakId;

    @JsonProperty("TILTAKSKODE")
    private String tiltakskode;

    @JsonProperty("ANTALL_DELTAKERE")
    private Integer antallDeltakere;

    @JsonProperty("ANTALL_VARIGHET")
    private Integer antallVarighet;

    @JsonProperty("DATO_FRA")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoFra;

    @JsonProperty("DATO_TIL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoTil;

    @JsonProperty("FAGPLANKODE")
    private String fagplankode;

    @JsonProperty("MAALEENHET_VARIGHET")
    private String maaleenhetVarighet;

    @JsonProperty("TEKST_FAGBESKRIVELSE")
    private String tekstFagbeskrivelse;

    @JsonProperty("TEKST_KURSSTED")
    private String tekstKurssted;

    @JsonProperty("TEKST_MAALGRUPPE")
    private String tekstMaalgruppe;

    @JsonProperty("STATUS_TREVERDIKODE_INNSOKNING")
    @JsonDeserialize(using = ArenaBooleanDeserializer.class)
    private Boolean statusTreverdikodeInnsokning;

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

    @JsonProperty("LOKALTNAVN")
    private String lokaltnavn;

    @JsonProperty("TILTAKSTATUSKODE")
    private Tiltakstatuskode tiltakstatuskode;

    @JsonProperty("PROSENT_DELTID")
    private Integer prosentDeltid;

    @JsonProperty("KOMMENTAR")
    private String kommentar;

    @JsonProperty("ARBGIV_ID_ARRANGOR")
    private Integer arbgivIdArrangor;

    @JsonProperty("PROFILELEMENT_ID_GEOGRAFI")
    private String profilelementIdGeografi;

    @JsonProperty("KLOKKETID_FREMMOTE")
    private String klokketidFremmote;

    @JsonProperty("DATO_FREMMOTE")
    private String datoFremmote;

    @JsonProperty("BEGRUNNELSE_STATUS")
    private String begrunnelseStatus;

    @JsonProperty("AVTALE_ID")
    private Integer avtaleId;

    @JsonProperty("AKTIVITET_ID")
    private Integer aktivitetId;

    @JsonProperty("DATO_INNSOKNINGSTART")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datoInnsokningstart;

    @JsonProperty("GML_FRA_DATO")
    private String gmlFraDato;

    @JsonProperty("GML_TIL_DATO")
    private String gmlTilDato;

    @JsonProperty("AETAT_FREMMOTEREG")
    private String aetatFremmotereg;

    @JsonProperty("AETAT_KONTERINGSSTED")
    private String aetatKonteringssted;

    @JsonProperty("OPPLAERINGNIVAAKODE")
    private String opplaeringnivaakode;

    @JsonProperty("TILTAKGJENNOMFORING_ID_REL")
    private String tiltakgjennomforingIdRel;

    @JsonProperty("VURDERING_GJENNOMFORING")
    private String vurderingGjennomforing;

    @JsonProperty("PROFILELEMENT_ID_OPPL_TILTAK")
    private Integer profilelementIdOpplTiltak;

    @JsonProperty("DATO_OPPFOLGING_OK")
    private String datoOppfolgingOk;

    @JsonProperty("PARTISJON")
    private String partisjon;

    @JsonProperty("MAALFORM_KRAVBREV")
    private String maalformKravbrev;

    @JsonProperty("EKSTERN_ID")
    private String eksternId;

    public boolean isArbeidstrening() {
        return ARBEIDSTRENING.equals(tiltakskode);
    }


}
