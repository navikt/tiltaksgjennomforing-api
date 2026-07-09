package no.nav.tag.tiltaksgjennomforing.enhet;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
public enum Kvalifiseringsgruppe {
    SPESIELT_TILPASSET_INNSATS("BATT", Innsatsgruppe.TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE),   // Personen har nedsatt arbeidsevne og har et identifisert behov for kvalifisering og/eller tilrettelegging. Aktivitetsplan skal utformes.
    SITUASJONSBESTEMT_INNSATS("BFORM", Innsatsgruppe.TRENGER_VEILEDNING),                       // Personen har moderat bistandsbehov
    VARIG_TILPASSET_INNSATS("VARIG", Innsatsgruppe.LITEN_MULIGHET_TIL_A_JOBBE),                 // Personen har varig nedsatt arbeidsevne
    GRADERT_VARIG_TILPASSET_INNSATS("VARIG", Innsatsgruppe.JOBBE_DELVIS),                       // Personen har varig nedsatt arbeidsevne - Kun i ny løsning (innsatsgruppe)
    STANDARD_INNSATS("IKVAL", Innsatsgruppe.GODE_MULIGHETER),                                   // Personen har behov for ordinær bistand
    BEHOV_FOR_ARBEIDSEVNEVURDERING("BKART", Innsatsgruppe.UKJENT),                              // Personen har behov for arbeidsevnevurdering
    IKKE_VURDERT("IVURD", Innsatsgruppe.UKJENT),                                                // Ikke vurdert
    RETTIGHETER_ETTER_FTRL_KAP11("KAP11", Innsatsgruppe.UKJENT),                                // Rettigheter etter Ftrl. Kapittel 11
    HELSERELATERT_ARBEIDSRETTET_OPPFOLGING_I_NAV("OPPFI", Innsatsgruppe.UKJENT),                // Helserelatert arbeidsrettet oppfølging i Nav
    SYKMELDT_OPPFOLGING_PA_ARBEIDSPLASSEN("VURDI", Innsatsgruppe.UKJENT),                       // Sykmeldt, oppfølging på arbeidsplassen
    SYKMELDT_UTEN_ARBEIDSGIVER("VURDU", Innsatsgruppe.UKJENT);                                  // Sykmeldt uten arbeidsgiver

    private final String kvalifiseringskode;
    private final Innsatsgruppe innsatsgruppe;

    Kvalifiseringsgruppe(String kvalifiseringskode, Innsatsgruppe innsatsgruppe) {
        this.kvalifiseringskode = kvalifiseringskode;
        this.innsatsgruppe = innsatsgruppe;
    }

    @JsonValue
    public String getKvalifiseringskode() {
        return kvalifiseringskode;
    }

    public static Kvalifiseringsgruppe parse(String kode) {
        return Arrays.stream(Kvalifiseringsgruppe.values())
            .filter(gruppe -> gruppe.getKvalifiseringskode().equals(kode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ukjent kvalifiseringsgruppe: " + kode));
    }

    public boolean isUgyldigKvalifiseringsgruppe() {
        return switch (this) {
            case STANDARD_INNSATS, BEHOV_FOR_ARBEIDSEVNEVURDERING, IKKE_VURDERT -> true;
            case RETTIGHETER_ETTER_FTRL_KAP11, HELSERELATERT_ARBEIDSRETTET_OPPFOLGING_I_NAV,
                 SYKMELDT_OPPFOLGING_PA_ARBEIDSPLASSEN, SYKMELDT_UTEN_ARBEIDSGIVER,
                 SPESIELT_TILPASSET_INNSATS, SITUASJONSBESTEMT_INNSATS, VARIG_TILPASSET_INNSATS,
                 GRADERT_VARIG_TILPASSET_INNSATS -> false;
        };
    }

    public boolean isKvalifisererTilMidlertidiglonnstilskuddOgSommerjobbOgMentor() {
        return switch (this) {
            case SPESIELT_TILPASSET_INNSATS, SITUASJONSBESTEMT_INNSATS, VARIG_TILPASSET_INNSATS,
                 GRADERT_VARIG_TILPASSET_INNSATS -> true;
            default -> false;
        };
    }

    public boolean isKvalifisererTilFirearigLonnstilskuddForUnge() {
        return switch (this) {
            case SPESIELT_TILPASSET_INNSATS, VARIG_TILPASSET_INNSATS, GRADERT_VARIG_TILPASSET_INNSATS -> true;
            default -> false;
        };
    }

    public boolean isKvalifisererTilVariglonnstilskudd() {
        return this == VARIG_TILPASSET_INNSATS;
    }

    public boolean isKvalifisererTilVTAO(){
        return this == VARIG_TILPASSET_INNSATS;
    }

    public Integer finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(Integer prosentsatsLiten, Integer prosentsatsStor) {
        return switch (this) {
            case SPESIELT_TILPASSET_INNSATS, VARIG_TILPASSET_INNSATS, GRADERT_VARIG_TILPASSET_INNSATS -> prosentsatsStor;
            case SITUASJONSBESTEMT_INNSATS -> prosentsatsLiten;
            default -> {
                log.warn("feilet med setting av kvalifiseringsgruppe. Kvalifiseringsgruppe: {}", this);
                yield null;
            }
        };
    }
}
