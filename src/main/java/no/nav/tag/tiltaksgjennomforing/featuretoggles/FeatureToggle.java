package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FeatureToggle {
    SMS_TIL_MOBILNUMMER("sms-til-mobilnummer"),
    ARENA_AVTALE_JOBB("arenaAvtaleJobb"),
    ARENA_PROSESSERINGS_JOBB("arenaProsesseringsJobb"),
    ARENA_CLEAN_UP_JOB("arenaCleanUpJobb"),
    ARENA_EREG_SJEKK("arenaEregSjekk"),
    ARENA_OPPFOLGING_SJEKK("arenaOppfolgingSjekk"),
    ARENA_KAFKA("arenaKafka"),
    PABEGYNT_AVTALE_RYDDE_JOBB("pabegyntAvtaleRyddeJobb"),
    KODE_6_SPERRE("kode6Sperre"),
    SJEKK_OM_DELTAKER_KAN_MOTTA_POST("sjekkOmDeltakerKanMottaPost"),
    MIGRERING_SKRIVEBESKYTTET("migreringSkrivebeskyttet"),
    FIREARIG_LONNSTILSKUDD("firearigLonnstilskudd"),
    REFUSJON_KLAR_I_TILTAK_NOTIFIKASJON("refusjon-klar-i-tiltak-notifikasjon"),
    VTAO_VEILEDER_TILGANG("vtaoVeilederTilgang"),
    VIS_HVEM_HAR_GODKJENT("visHvemHarGodkjent"),
    VIS_NEDETID_BANNER("visNedetidBanner");

    private String toggleNavn;

    FeatureToggle(String toggleNavn) {
        this.toggleNavn = toggleNavn;
    }

    @JsonValue
    public String getToggleNavn() {
        return toggleNavn;
    }
}
