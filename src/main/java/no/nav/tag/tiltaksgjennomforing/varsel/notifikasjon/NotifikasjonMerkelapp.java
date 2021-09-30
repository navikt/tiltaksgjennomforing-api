package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonMerkelapp {
    TILTAK_AVTALE_OPPRETTET(""),
    TILTAK_AVTALE_MANGLER_GODKJENNING("");

    private final String merkelapp;

    NotifikasjonMerkelapp(String merkelapp) {
        this.merkelapp = merkelapp;
    }

    public String getValue() {
        return merkelapp;
    }
}
