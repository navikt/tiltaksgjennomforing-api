package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.Now;

public enum Status {
    ANNULLERT("Annullert"),
    PÅBEGYNT("Påbegynt"),
    MANGLER_GODKJENNING("Mangler godkjenning"),
    KLAR_FOR_OPPSTART("Klar for oppstart"),
    GJENNOMFØRES("Gjennomføres"),
    AVSLUTTET("Avsluttet");

    private final String beskrivelse;

    boolean erAvsluttetEllerAnnullert() {
        return this.equals(ANNULLERT) || this.equals(AVSLUTTET);
    }

    Status(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public static Status fra(Avtale avtale) {
        if (avtale.getAnnullertTidspunkt() != null) {
            return Status.ANNULLERT;
        } else if (avtale.erAvtaleInngått() && (avtale.getGjeldendeInnhold()
            .getSluttDato()
            .isBefore(Now.localDate()))) {
            return Status.AVSLUTTET;
        } else if (avtale.erAvtaleInngått() && (avtale.getGjeldendeInnhold()
            .getStartDato()
            .isBefore(Now.localDate().plusDays(1)))) {
            return Status.GJENNOMFØRES;
        } else if (avtale.erAvtaleInngått()) {
            return Status.KLAR_FOR_OPPSTART;
        } else if (avtale.felterSomIkkeErFyltUt().isEmpty()) {
            return Status.MANGLER_GODKJENNING;
        } else {
            return Status.PÅBEGYNT;
        }
    }
}
