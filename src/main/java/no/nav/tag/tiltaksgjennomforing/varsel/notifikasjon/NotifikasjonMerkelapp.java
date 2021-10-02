package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonMerkelapp {
    TILTAK("Tiltak"),
    REFUSJON("Refusjon");

    private final String merkelapp;

    NotifikasjonMerkelapp(String merkelapp) {
        this.merkelapp = merkelapp;
    }

    public String getValue() {
        return merkelapp;
    }
}
