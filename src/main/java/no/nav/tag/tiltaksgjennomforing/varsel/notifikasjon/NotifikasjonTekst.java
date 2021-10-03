package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonTekst {
    AVTALE_OPPRETTET("Ny avtale opprettet. Åpne avtale og fyll ut innholdet."),
    TILTAK_AVTALE_MANGLER_GODKJENNING("Tiltaksavtale mangler godkjenning."),
    TILTAK_AVTALE_ENDRET("Tiltak avtale endret"),
    TILTAK_AVTALE_INNGATT("Arbeidstiltak avtale inngått."),
    TILTAK_AVTALE_AVBRUTT("Arbeidstiltak avtale avbrutt."),
    TILTAK_AVTALE_LASTOPP("Avtale har blitt låst opp. Trenger ny godkjenning"),
    TILTAK_AVTALE_GJENOPPRETTET("Arbeidstiltak avtale gjenopprettet."),
    TILTAK_AVTALE_KLAR_REFUSJON("Refusjon for tiltaksavtale er klar."),
    TILTAK_AVTALE_GODKJENT_VEILEDER("Arbeidstiltak avtale godkjent av veileder.");

    private final String tekst;

    NotifikasjonTekst(String tekst) {
        this.tekst = tekst;
    }

    public String getTekst() {
        return tekst;
    }
}
