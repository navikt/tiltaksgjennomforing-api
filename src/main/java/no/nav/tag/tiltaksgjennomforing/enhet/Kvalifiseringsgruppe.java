package no.nav.tag.tiltaksgjennomforing.enhet;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public enum Kvalifiseringsgruppe {
    SPESIELT_TILPASSET_INNSATS("BATT"),      // Personen har nedsatt arbeidsevne og har et identifisert behov for kvalifisering og/eller tilrettelegging. Aktivitetsplan skal utformes.
    SITUASJONSBESTEMT_INNSATS("BFORM"),      // Personen har moderat bistandsbehov
    VARIG_TILPASSET_INNSATS("VARIG"),        // Personen har varig nedsatt arbeidsevne
    BEHOV_FOR_ARBEIDSEVNEVURDERING("BKART"), // Personen har behov for arbeidsevnevurdering
    STANDARD_INNSATS("IKVAL"),               // Personen har behov for ordinær bistand
    IKKE_VURDERT("IVURD"),                   // Ikke vurdert
    RETTIGHETER_ETTER_FTRL_KAP11("KAP11"),   // Rettigheter etter Ftrl. Kapittel 11
    HELSERELATERT_ARBEIDSRETTET_OPPFOLGING_I_NAV("OPPFI"), // Helserelatert arbeidsrettet oppfølging i NAV
    SYKMELDT_OPPFOLGING_PA_ARBEIDSPLASSEN("VURDI"),        // Sykmeldt, oppfølging på arbeidsplassen
    SYKMELDT_UTEN_ARBEIDSGIVER("VURDU");                   // Sykmeldt uten arbeidsgiver


    private final String kvalifiseringskode;

    Kvalifiseringsgruppe(String kvalifiseringskode) {
        this.kvalifiseringskode = kvalifiseringskode;
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
                    SPESIELT_TILPASSET_INNSATS, SITUASJONSBESTEMT_INNSATS, VARIG_TILPASSET_INNSATS -> false;
        };
    }

    public boolean isKvalifisererTilMidlertidiglonnstilskuddOgSommerjobbOgMentor() {
        return switch (this) {
            case SPESIELT_TILPASSET_INNSATS, SITUASJONSBESTEMT_INNSATS, VARIG_TILPASSET_INNSATS -> true;
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
            case SPESIELT_TILPASSET_INNSATS, VARIG_TILPASSET_INNSATS -> prosentsatsStor;
            case SITUASJONSBESTEMT_INNSATS -> prosentsatsLiten;
            default -> {
                log.warn("feilet med setting av kvalifiseringsgruppe. Kvalifiseringsgruppe: {}", this);
                yield null;
            }
        };
    }
}
