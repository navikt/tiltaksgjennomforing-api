package no.nav.tag.tiltaksgjennomforing.arena.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ArenaDtoDeserializerTest {

    private static final String TILTAKSGJENNOMFORING_ENDRET_JSON = """
        {
          "table": "SIAMO.TILTAKGJENNOMFORING",
          "op_type": "U",
          "op_ts": "2024-05-02 00:00:16.000000",
          "current_ts": "2024-05-02 00:00:22.002000",
          "pos": "00000000660086288130",
          "before": {
            "TILTAKGJENNOMFORING_ID": 3821904,
            "SAK_ID": 13673239,
            "TILTAKSKODE": "JOBBK",
            "ANTALL_DELTAKERE": 15,
            "ANTALL_VARIGHET": null,
            "DATO_FRA": "2024-03-01 00:00:00",
            "DATO_TIL": "2024-05-01 00:00:00",
            "FAGPLANKODE": null,
            "MAALEENHET_VARIGHET": null,
            "TEKST_FAGBESKRIVELSE": "dfesfse",
            "TEKST_KURSSTED": null,
            "TEKST_MAALGRUPPE": "fesfse",
            "STATUS_TREVERDIKODE_INNSOKNING": "J",
            "REG_DATO": "2024-03-01 15:37:42",
            "REG_USER": "SM03331",
            "MOD_DATO": "2024-04-06 00:00:07",
            "MOD_USER": "ARENA_TI",
            "LOKALTNAVN": "Sindres gjennomføring for å teste åpent for innsøk",
            "TILTAKSTATUSKODE": "GJENNOMFOR",
            "PROSENT_DELTID": 100,
            "KOMMENTAR": null,
            "ARBGIV_ID_ARRANGOR": 141274,
            "PROFILELEMENT_ID_GEOGRAFI": null,
            "KLOKKETID_FREMMOTE": null,
            "DATO_FREMMOTE": null,
            "BEGRUNNELSE_STATUS": null,
            "AVTALE_ID": 328961,
            "AKTIVITET_ID": 134021320,
            "DATO_INNSOKNINGSTART": null,
            "GML_FRA_DATO": null,
            "GML_TIL_DATO": null,
            "AETAT_FREMMOTEREG": "0331",
            "AETAT_KONTERINGSSTED": "0331",
            "OPPLAERINGNIVAAKODE": null,
            "TILTAKGJENNOMFORING_ID_REL": null,
            "PROFILELEMENT_ID_OPPL_TILTAK": 130512,
            "DATO_OPPFOLGING_OK": null,
            "PARTISJON": null,
            "MAALFORM_KRAVBREV": "NO",
            "EKSTERN_ID": "2fbdcbec-fee7-419e-b05e-33da0522d909"
          },
          "after":{
            "TILTAKGJENNOMFORING_ID": 3821904,
            "SAK_ID": 13673239,
            "TILTAKSKODE": "JOBBK",
            "ANTALL_DELTAKERE": 15,
            "ANTALL_VARIGHET": null,
            "DATO_FRA": "2024-03-01 00:00:00",
            "DATO_TIL": "2024-05-01 00:00:00",
            "FAGPLANKODE": null,
            "MAALEENHET_VARIGHET": null,
            "TEKST_FAGBESKRIVELSE": "dfesfse",
            "TEKST_KURSSTED": null,
            "TEKST_MAALGRUPPE": "fesfse",
            "STATUS_TREVERDIKODE_INNSOKNING": "J",
            "REG_DATO": "2024-03-01 15:37:42",
            "REG_USER": "SM03331",
            "MOD_DATO": "2024-05-02 00:00:10",
            "MOD_USER": "TILTADM",
            "LOKALTNAVN": "Sindres gjennomføring for å teste åpent for innsøk",
            "TILTAKSTATUSKODE": "AVSLUTT",
            "PROSENT_DELTID": 100,
            "KOMMENTAR": null,
            "ARBGIV_ID_ARRANGOR": 141274,
            "PROFILELEMENT_ID_GEOGRAFI": null,
            "KLOKKETID_FREMMOTE": null,
            "DATO_FREMMOTE": null,
            "BEGRUNNELSE_STATUS": null,
            "AVTALE_ID": 328961,
            "AKTIVITET_ID": 134021320,
            "DATO_INNSOKNINGSTART": null,
            "GML_FRA_DATO": null,
            "GML_TIL_DATO": null,
            "AETAT_FREMMOTEREG": "0331",
            "AETAT_KONTERINGSSTED": "0331",
            "OPPLAERINGNIVAAKODE": null,
            "TILTAKGJENNOMFORING_ID_REL": null,
            "VURDERING_GJENNOMFORING": null,
            "PROFILELEMENT_ID_OPPL_TILTAK": 130512,
            "DATO_OPPFOLGING_OK": null,
            "PARTISJON": null,
            "MAALFORM_KRAVBREV": "NO",
            "EKSTERN_ID": "2fbdcbec-fee7-419e-b05e-33da0522d909"
          }
        }
    """;

    private static final String TILTAKSSAK_ENDRET_JSON = """
        {
          "table": "SIAMO.SAK",
          "op_type": "U",
          "op_ts": "2024-04-29 09:30:55.000000",
          "current_ts": "2024-04-29 09:30:58.855002",
          "pos": "00000000660071631852",
          "before":{
            "SAK_ID": 13426397,
            "SAKSKODE": "TILT",
            "REG_DATO": "2021-06-25 08:48:38",
            "REG_USER": "OFC1500",
            "MOD_DATO": "2021-06-25 08:48:38",
            "MOD_USER": "OFC1500",
            "TABELLNAVNALIAS": "SAK",
            "OBJEKT_ID": 13506727,
            "AAR": 2021,
            "LOPENRSAK": 232749,
            "DATO_AVSLUTTET": null,
            "SAKSTATUSKODE": "AKTIV",
            "ARKIVNOKKEL": "532",
            "AETATENHET_ARKIV": null,
            "ARKIVHENVISNING": null,
            "BRUKERID_ANSVARLIG": "ABC1500",
            "AETATENHET_ANSVARLIG": "1500",
            "OBJEKT_KODE": null,
            "STATUS_ENDRET": "2021-06-25 08:48:38",
            "PARTISJON": null,
            "ER_UTLAND": "N"
          },
          "after":{
            "SAK_ID": 13426397,
            "SAKSKODE": "TILT",
            "REG_DATO": "2021-06-25 08:48:38",
            "REG_USER": "OFC1500",
            "MOD_DATO": "2024-04-29 09:30:49",
            "MOD_USER": "TILTADM",
            "TABELLNAVNALIAS": "SAK",
            "OBJEKT_ID": 13506727,
            "AAR": 2021,
            "LOPENRSAK": 232749,
            "DATO_AVSLUTTET": null,
            "SAKSTATUSKODE": "AKTIV",
            "ARKIVNOKKEL": "532",
            "AETATENHET_ARKIV": null,
            "ARKIVHENVISNING": null,
            "BRUKERID_ANSVARLIG": "ABC1500",
            "AETATENHET_ANSVARLIG": "1500",
            "OBJEKT_KODE": null,
            "STATUS_ENDRET": "2024-04-29 09:30:49",
            "PARTISJON": null,
            "ER_UTLAND": "N"
          }
        }
    """;

    private static final String TILTAKDELTAKER_ENDRET_JSON = """
        {
           "table": "SIAMO.TILTAKDELTAKER",
           "op_type": "U",
           "op_ts": "2024-05-31 12:00:20.000000",
           "current_ts": "2024-05-31 12:00:25.818001",
           "pos": "00000000670035828007",
           "before": {
             "TILTAKDELTAKER_ID": 6419915,
             "PERSON_ID": 4934594,
             "TILTAKGJENNOMFORING_ID": 3708348,
             "DELTAKERSTATUSKODE": "AKTUELL",
             "DELTAKERTYPEKODE": "INNSOKT",
             "AARSAKVERDIKODE_STATUS": null,
             "OPPMOTETYPEKODE": null,
             "PRIORITET": null,
             "BEGRUNNELSE_INNSOKT": "Syntetisert rettighet",
             "BEGRUNNELSE_PRIORITERING": null,
             "REG_DATO": "2024-05-31 12:00:14",
             "REG_USER": "AJB1813",
             "MOD_DATO": "2024-05-31 12:00:14",
             "MOD_USER": "AJB1813",
             "DATO_SVARFRIST": null,
             "DATO_FRA": "2021-06-01 00:00:00",
             "DATO_TIL": "2021-08-31 00:00:00",
             "BEGRUNNELSE_STATUS": null,
             "PROSENT_DELTID": 50,
             "BRUKERID_STATUSENDRING": "AJB1813",
             "DATO_STATUSENDRING": "2024-05-31 12:00:14",
             "AKTIVITET_ID": 134025078,
             "BRUKERID_ENDRING_PRIORITERING": null,
             "DATO_ENDRING_PRIORITERING": null,
             "DOKUMENTKODE_SISTE_BREV": null,
             "STATUS_INNSOK_PAKKE": null,
             "STATUS_OPPTAK_PAKKE": null,
             "OPPLYSNINGER_INNSOK": null,
             "PARTISJON": null,
             "BEGRUNNELSE_BESTILLING": null,
             "ANTALL_DAGER_PR_UKE": null
           },
           "after": {
             "TILTAKDELTAKER_ID": 6419915,
             "PERSON_ID": 4934594,
             "TILTAKGJENNOMFORING_ID": 3708348,
             "DELTAKERSTATUSKODE": "TILBUD",
             "DELTAKERTYPEKODE": "INNSOKT",
             "AARSAKVERDIKODE_STATUS": null,
             "OPPMOTETYPEKODE": null,
             "PRIORITET": null,
             "BEGRUNNELSE_INNSOKT": "Syntetisert rettighet",
             "BEGRUNNELSE_PRIORITERING": null,
             "REG_DATO": "2024-05-31 12:00:14",
             "REG_USER": "AJB1813",
             "MOD_DATO": "2024-05-31 12:00:14",
             "MOD_USER": "AJB1813",
             "DATO_SVARFRIST": null,
             "DATO_FRA": "2021-06-01 00:00:00",
             "DATO_TIL": "2021-08-31 00:00:00",
             "BEGRUNNELSE_STATUS": null,
             "PROSENT_DELTID": 50,
             "BRUKERID_STATUSENDRING": "AJB1813",
             "DATO_STATUSENDRING": "2024-05-31 12:00:14",
             "AKTIVITET_ID": 134025078,
             "BRUKERID_ENDRING_PRIORITERING": null,
             "DATO_ENDRING_PRIORITERING": null,
             "DOKUMENTKODE_SISTE_BREV": null,
             "STATUS_INNSOK_PAKKE": null,
             "STATUS_OPPTAK_PAKKE": null,
             "OPPLYSNINGER_INNSOK": null,
             "PARTISJON": null,
             "BEGRUNNELSE_BESTILLING":null,
             "ANTALL_DAGER_PR_UKE":null
           }
         }
    """;

    private ObjectMapper mapper;

    @BeforeEach
    public void beforeEach() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void deserialize_og_serialize_tiltakgjennomforing_endret_kafka_melding() throws IOException {
        TypeReference<ArenaKafkaMessage<TiltakgjennomforingEndretDto>> typeReference = new TypeReference<>() {};

        ArenaKafkaMessage<TiltakgjennomforingEndretDto> tiltakgjennomforingEndret = mapper.readValue(
            TILTAKSGJENNOMFORING_ENDRET_JSON,
            typeReference
        );

        assertThat(tiltakgjennomforingEndret.after().tiltakgjennomforingId()).isEqualTo(3821904);

        String jsonStr = mapper.writeValueAsString(tiltakgjennomforingEndret);
        assertThat(jsonStr).contains("SIAMO.TILTAKGJENNOMFORING");
        assertThat(jsonStr).contains("before");
        assertThat(jsonStr).contains("after");
    }

    @Test
    public void deserialize_og_serialize_tiltaksakendret_endret_kafka_melding() throws IOException {
        TypeReference<ArenaKafkaMessage<TiltaksakEndretDto>> typeReference = new TypeReference<>() {};

        ArenaKafkaMessage<TiltaksakEndretDto> tiltaksakendret = mapper.readValue(
            TILTAKSSAK_ENDRET_JSON,
            typeReference
        );

        assertThat(tiltaksakendret.after().sakId()).isEqualTo(13426397);

        String jsonStr = mapper.writeValueAsString(tiltaksakendret);
        assertThat(jsonStr).contains("SIAMO.SAK");
        assertThat(jsonStr).contains("before");
        assertThat(jsonStr).contains("after");
    }

    @Test
    public void deserialize_og_serialize_tiltakdeltaker_endret_kafka_melding() throws IOException {
        TypeReference<ArenaKafkaMessage<TiltakdeltakerEndretDto>> typeReference = new TypeReference<>() {};

        ArenaKafkaMessage<TiltakdeltakerEndretDto> tiltakdeltakerEndret = mapper.readValue(
            TILTAKDELTAKER_ENDRET_JSON,
            typeReference
        );

        assertThat(tiltakdeltakerEndret.after().tiltakdeltakerId()).isEqualTo(6419915);

        String jsonStr = mapper.writeValueAsString(tiltakdeltakerEndret);
        assertThat(jsonStr).contains("SIAMO.TILTAKDELTAKER");
        assertThat(jsonStr).contains("before");
        assertThat(jsonStr).contains("after");
    }
}
