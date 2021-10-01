package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonTekst {
    AVTALE_OPPRETTET("Ny arbeidstiltak avtale opprettet"),
    AVTALE_TRENGER_GODKJENNING("Avtale trenger godkjenning"),
    TILTAK_AVTALE_OPPRETTET("Arbeidstiltak avtale opprettet"),
    TILTAK_AVTALE_MANGLER_GODKJENNING("Arbeidstiltak avtale mangler godkjenning"),
    TILTAK_AVTALE_ENDRET("Arbeidstiltak avtale endret"),
    TILTAK_AVTALE_INNGATT("Arbeidstiltak avtale inngått"),
    TILTAK_AVTALE_AVBRUTT("Arbeidstiltak avtale avbrutt"),
    TILTAK_AVTALE_LASTOPP("Arbeidstiltak avtale har blitt låst opp"),
    TILTAK_AVTALE_GJENOPPRETTET("Arbeidstiltak avtale gjenopprettet"),
    TILTAK_AVTALE_KLAR_REFUSJON("Refusjon for tiltaksavtale er klar"),
    TILTAK_AVTALE_GODKJENT_VEILEDER("Arbeidstiltak avtale godkjent av veileder");

    private final String tekst;

    NotifikasjonTekst(String tekst) {
        this.tekst = tekst;
    }

    public String getTekst() {
        return tekst;
    }
}
