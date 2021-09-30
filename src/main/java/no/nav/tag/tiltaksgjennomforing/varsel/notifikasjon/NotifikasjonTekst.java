package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonTekst {
    AVTALE_OPPRETTET("Ny arbeidstiltak avtale opprettet"),
    AVTALE_TRENGER_GODKJENNING("Avtale trenger godkjenning");

    private final String tekst;

    NotifikasjonTekst(String tekst) {
        this.tekst = tekst;
    }

    public String getTekst() {
        return tekst;
    }
}
