package no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Innsatsgruppe {
    GODE_MULIGHETER("GODE_MULIGHETER", "STANDARD_INNSATS", "IKVAL", "Gode muligheter"),
    TRENGER_VEILEDNING("TRENGER_VEILEDNING", "SITUASJONSBESTEMT_INNSATS", "BFORM", "Trenger veiledning"),
    TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE("TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE", "SPESIELT_TILPASSET_INNSATS", "BATT", "Trenger veiledning, nedsatt arbeidsevne"),
    JOBBE_DELVIS("JOBBE_DELVIS", "GRADERT_VARIG_TILPASSET_INNSATS", "VARIG", "Jobbe delvis"),
    LITEN_MULIGHET_TIL_A_JOBBE("LITEN_MULIGHET_TIL_A_JOBBE", "VARIG_TILPASSET_INNSATS", "VARIG", "Liten mulighet til å jobbe");

    private final String kode;
    private final String gammelKode;
    private final String arenaKode;
    private final String beskrivelse;
}
