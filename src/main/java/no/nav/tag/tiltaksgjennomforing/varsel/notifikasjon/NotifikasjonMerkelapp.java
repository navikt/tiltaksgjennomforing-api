package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonMerkelapp {
    TILTAK_AVTALE_OPPRETTET("Tiltak opprettet"),
    TILTAK_AVTALE_MANGLER_GODKJENNING("Tiltak mangler godkjenning"),
    TILTAK_AVTALE_ENDRET("Tiltak endret"),
    TILTAK_AVTALE_INNGATT("Tiltak inngått"),
    TILTAK_AVTALE_AVBRUTT("Tiltak avbrutt"),
    TILTAK_AVTALE_LASTOPP("Tiltak låst opp"),
    TILTAK_AVTALE_GJENOPPRETTET("Tiltak gjenopprettet"),
    TILTAK_AVTALE_KLAR_REFUSJON("Refusjon klar"),
    TILTAK_AVTALE_GODKJENT_VEILEDER("Tiltak godkjent");

    // godkjentAvVeileder
    private final String merkelapp;

    NotifikasjonMerkelapp(String merkelapp) {
        this.merkelapp = merkelapp;
    }

    public String getValue() {
        return merkelapp;
    }
}
