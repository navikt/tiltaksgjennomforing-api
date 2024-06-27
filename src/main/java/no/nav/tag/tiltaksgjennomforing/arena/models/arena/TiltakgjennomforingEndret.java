package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.nav.tag.tiltaksgjennomforing.arena.utils.ArenaBooleanDeserializer;

import java.time.LocalDateTime;

public record TiltakgjennomforingEndret(
    @JsonProperty("TILTAKGJENNOMFORING_ID")
    Integer tiltakgjennomforingId,

    @JsonProperty("SAK_ID")
    Integer sakId,

    @JsonProperty("TILTAKSKODE")
    String tiltakskode,

    @JsonProperty("ANTALL_DELTAKERE")
    Integer antallDeltakere,

    @JsonProperty("ANTALL_VARIGHET")
    Integer antallVarighet,

    @JsonProperty("DATO_FRA")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoFra,

    @JsonProperty("DATO_TIL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoTil,

    @JsonProperty("FAGPLANKODE")
    String fagplankode,

    @JsonProperty("MAALEENHET_VARIGHET")
    String maaleenhetVarighet,

    @JsonProperty("TEKST_FAGBESKRIVELSE")
    String tekstFagbeskrivelse,

    @JsonProperty("TEKST_KURSSTED")
    String tekstKurssted,

    @JsonProperty("TEKST_MAALGRUPPE")
    String tekstMaalgruppe,

    @JsonProperty("STATUS_TREVERDIKODE_INNSOKNING")
    @JsonDeserialize(using = ArenaBooleanDeserializer.class)
    Boolean statusTreverdikodeInnsokning,

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

    @JsonProperty("LOKALTNAVN")
    String lokaltnavn,

    @JsonProperty("TILTAKSTATUSKODE")
    Tiltakstatuskode tiltakstatuskode,

    @JsonProperty("PROSENT_DELTID")
    Integer prosentDeltid,

    @JsonProperty("KOMMENTAR")
    String kommentar,

    @JsonProperty("ARBGIV_ID_ARRANGOR")
    Integer arbgivIdArrangor,

    @JsonProperty("PROFILELEMENT_ID_GEOGRAFI")
    String profilelementIdGeografi,

    @JsonProperty("KLOKKETID_FREMMOTE")
    String klokketidFremmote,

    @JsonProperty("DATO_FREMMOTE")
    String datoFremmote,

    @JsonProperty("BEGRUNNELSE_STATUS")
    String begrunnelseStatus,

    @JsonProperty("AVTALE_ID")
    Integer avtaleId,

    @JsonProperty("AKTIVITET_ID")
    Integer aktivitetId,

    @JsonProperty("DATO_INNSOKNINGSTART")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime datoInnsokningstart,

    @JsonProperty("GML_FRA_DATO")
    String gmlFraDato,

    @JsonProperty("GML_TIL_DATO")
    String gmlTilDato,

    @JsonProperty("AETAT_FREMMOTEREG")
    String aetatFremmotereg,

    @JsonProperty("AETAT_KONTERINGSSTED")
    String aetatKonteringssted,

    @JsonProperty("OPPLAERINGNIVAAKODE")
    String opplaeringnivaakode,

    @JsonProperty("TILTAKGJENNOMFORING_ID_REL")
    String tiltakgjennomforingIdRel,

    @JsonProperty("VURDERING_GJENNOMFORING")
    String vurderingGjennomforing,

    @JsonProperty("PROFILELEMENT_ID_OPPL_TILTAK")
    Integer profilelementIdOpplTiltak,

    @JsonProperty("DATO_OPPFOLGING_OK")
    String datoOppfolgingOk,

    @JsonProperty("PARTISJON")
    String partisasjon,

    @JsonProperty("MAALFORM_KRAVBREV")
    String maalformKravbrev,

    @JsonProperty("EKSTERN_ID")
    String eksternId
){}
