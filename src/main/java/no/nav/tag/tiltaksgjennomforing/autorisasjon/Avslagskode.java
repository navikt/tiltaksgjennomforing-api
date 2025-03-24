package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.poao_tilgang.client.Decision;

public enum Avslagskode {
    STRENGT_FORTROLIG_ADRESSE,
    FORTROLIG_ADRESSE,
    EGNE_ANSATTE,
    MANGLER_TILGANG_TIL_AD_GRUPPE,
    POLICY_IKKE_IMPLEMENTERT,
    IKKE_TILGANG_FRA_ABAC,
    IKKE_TILGANG_TIL_NAV_ENHET,
    UKLAR_TILGANG_MANGLENDE_INFORMASJON,
    EKSTERN_BRUKER_HAR_IKKE_TILGANG,
    UKJENT,
    INGEN_RESPONS;

    public static Avslagskode parse(Decision decision) {
        if (decision.isPermit()) {
            throw new IllegalArgumentException("Kan ikke hente tilgangskode for en tillatelse");
        }

        Decision.Deny deny = (Decision.Deny) decision;
        return switch (deny.getReason()) {
            case "MANGLER_TILGANG_TIL_AD_GRUPPE" -> {
                if (deny.getMessage().contains("0000-GA-Strengt_Fortrolig_Adresse")) {
                    yield STRENGT_FORTROLIG_ADRESSE;
                }
                if (deny.getMessage().contains("0000-GA-Fortrolig_Adresse")) {
                    yield FORTROLIG_ADRESSE;
                }
                if (deny.getMessage().contains("0000-GA-Egne_ansatte")) {
                    yield EGNE_ANSATTE;
                }
                yield IKKE_TILGANG_TIL_NAV_ENHET;
            }
            case "POLICY_IKKE_IMPLEMENTERT" -> POLICY_IKKE_IMPLEMENTERT;
            case "IKKE_TILGANG_FRA_ABAC" -> IKKE_TILGANG_FRA_ABAC;
            case "IKKE_TILGANG_TIL_NAV_ENHET" -> IKKE_TILGANG_TIL_NAV_ENHET;
            case "UKLAR_TILGANG_MANGLENDE_INFORMASJON" -> UKLAR_TILGANG_MANGLENDE_INFORMASJON;
            case "EKSTERN_BRUKER_HAR_IKKE_TILGANG" -> EKSTERN_BRUKER_HAR_IKKE_TILGANG;
            default -> UKJENT;
        };
    }
}
