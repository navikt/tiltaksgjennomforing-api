package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonTekst {
    AVTALE_OPPRETTET("Ny arbeidstiltak avtale opprettet. Åpne avtale og fyll ut innholdet."),
    TILTAK_AVTALE_INNGATT("Arbeidstiltak avtale inngått."),
    TILTAK_AVTALE_KLAR_REFUSJON("Refusjon for arbeidstiltaksavtale er klar."),
    TILTAK_AVTALE_GODKJENT_VEILEDER("Arbeidstiltaksavtale godkjent av veileder."),
    TILTAK_STILLINGSBESKRIVELSE_ENDRET("Stillingsbeskrivelse i arbeidstiltak endret av veileder" ),
    TILTAK_MÅL_ENDRET("Mål i arbeidstiltaksavtale endret av veileder"),
    TILTAK_OPPFØLGING_OG_TILRETTELEGGING_ENDRET("Oppfølging og tilrettelegging rundt arbeidstiltak endret av veileder"),
    TILTAK_AVTALE_FORKORTET("Arbeidstiltak avtale forkortet"),
    TILTAK_AVTALE_FORLENGET("Arbeidstiltak avtale forlenget av veileder"),
    TILTAK_TILSKUDDSBEREGNING_ENDRET("Tilskuddsberegning i arbeidstiltak endret av veileder"),
    TILTAK_KONTAKTINFORMASJON_ENDRET("Kontaktinformasjon i arbeidstiltak endret av veileder");

    private final String tekst;

    NotifikasjonTekst(String tekst) {
        this.tekst = tekst;
    }

    public String getTekst() {
        return tekst;
    }
}
