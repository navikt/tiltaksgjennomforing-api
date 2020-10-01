package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Status {
    AVBRUTT("Avbrutt"),
    PÅBEGYNT("Påbegynt"),
    MANGLER_GODKJENNING("Mangler godkjenning"),
    KLAR_FOR_OPPSTART("Klar for oppstart"),
    GJENNOMFØRES("Gjennomføres"),
    AVSLUTTET("Avsluttet");

    private final String statusVerdi;

    Status(String statusVerdi) {
        this.statusVerdi = statusVerdi;
    }

    public String getStatusVerdi() {
        return statusVerdi;
    }
}
