package no.nav.tag.tiltaksgjennomforing.enhet;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceGruppe {
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


    private final String servicekode;

    ServiceGruppe(String servicekode) {
        this.servicekode = servicekode;
    }

    @JsonValue
    public String getServicekode() {
        return servicekode;
    }

    public static Boolean servicegruppeErRiktig(ServiceGruppe serviceGruppe) {
        return switch (serviceGruppe) {
            case SITUASJONSBESTEMT_INNSATS, SPESIELT_TILPASSET_INNSATS, VARIG_TILPASSET_INNSATS -> true;
            case BEHOV_FOR_ARBEIDSEVNEVURDERING, STANDARD_INNSATS, IKKE_VURDERT, RETTIGHETER_ETTER_FTRL_KAP11,
                    HELSERELATERT_ARBEIDSRETTET_OPPFOLGING_I_NAV, SYKMELDT_OPPFOLGING_PA_ARBEIDSPLASSEN,
                    SYKMELDT_UTEN_ARBEIDSGIVER -> false;
        };
    }
}
