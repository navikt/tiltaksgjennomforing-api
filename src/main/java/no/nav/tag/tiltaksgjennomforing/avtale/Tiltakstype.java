package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Tiltakstype {
    ARBEIDSTRENING("Arbeidstrening", "ab0422", "ARBTREN"),
    MIDLERTIDIG_LONNSTILSKUDD("Midlertidig lønnstilskudd", "ab0336", "MIDLONTIL"),
    VARIG_LONNSTILSKUDD("Varig lønnstilskudd", "ab0337", "VARLONTIL"),
    MENTOR("Mentor", "ab0416", "MENTOR"),
    SOMMERJOBB("Sommerjobb", "ab0450", null);

    final String navn;
    final String behandlingstema;
    final String tiltakskodeArena;

    Tiltakstype(String navn, String behandlingstema, String tiltakskodeArena) {
        this.navn = navn;
        this.behandlingstema = behandlingstema;
        this.tiltakskodeArena = tiltakskodeArena;
    }

    public String getNavn() {
        return this.navn;
    }

    public String getBehandlingstema() {
        return this.behandlingstema;
    }

    public String getTiltakskodeArena() {
        return tiltakskodeArena;
    }
}
