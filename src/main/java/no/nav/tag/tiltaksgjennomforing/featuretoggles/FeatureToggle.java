package no.nav.tag.tiltaksgjennomforing.featuretoggles;

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
    MIGRERING_SKRIVEBESKYTTET("migreringSkrivebeskyttet"),
    FIREARIG_LONNSTILSKUDD("firearigLonnstilskudd");

    private String toggleNavn;

    FeatureToggle(String toggleNavn) {
        this.toggleNavn = toggleNavn;
    }

    public String getToggleNavn() {
        return toggleNavn;
    }
}
