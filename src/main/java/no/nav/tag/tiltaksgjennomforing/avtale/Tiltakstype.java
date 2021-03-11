package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Tiltakstype {
    ARBEIDSTRENING("Arbeidstrening", "ab0422"),
    MIDLERTIDIG_LONNSTILSKUDD("Midlertidig lønnstilskudd", "ab0336"),
    VARIG_LONNSTILSKUDD("Varig lønnstilskudd", "ab0337"),
    MENTOR("Mentor", "ab0416"),
    SOMMERJOBB("Sommerjobb", "ukjent");

    final String navn;
    final String behandlingstema;

    Tiltakstype(String navn, String behandlingstema) {
        this.navn = navn;
        this.behandlingstema = behandlingstema;
    }

    public String getNavn() {
        return this.navn;
    }

    public String getBehandlingstema() {
        return this.behandlingstema;
    }
}
