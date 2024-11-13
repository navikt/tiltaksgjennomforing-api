package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Tiltakstype {
    ARBEIDSTRENING("Arbeidstrening", "ab0422", "ARBTREN"),
    MIDLERTIDIG_LONNSTILSKUDD("Midlertidig lønnstilskudd", "ab0336", "MIDLONTIL"),
    VARIG_LONNSTILSKUDD("Varig lønnstilskudd", "ab0337", "VARLONTIL"),
    MENTOR("Mentor", "ab0416", "MENTOR"),
    INKLUDERINGSTILSKUDD("Inkluderingstilskudd", "ab0417", "INKLUTILS"),
    SOMMERJOBB("Sommerjobb", "ab0450", null),
    VTAO("Varig tilrettelagt arbeid i ordinær virksomhet", "ab0418", "VATIAROR");

    final String beskrivelse;
    final String behandlingstema;
    final String tiltakskodeArena;

    Tiltakstype(String beskrivelse, String behandlingstema, String tiltakskodeArena) {
        this.beskrivelse = beskrivelse;
        this.behandlingstema = behandlingstema;
        this.tiltakskodeArena = tiltakskodeArena;
    }

    public String getBeskrivelse() {
        return this.beskrivelse;
    }

    public String getBehandlingstema() {
        return this.behandlingstema;
    }

    public String getTiltakskodeArena() {
        return tiltakskodeArena;
    }

    public boolean isMidlerTidiglonnstilskuddEllerSommerjobbEllerMentor() {
        return this == Tiltakstype.SOMMERJOBB ||
            this == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD ||
            this == Tiltakstype.MENTOR;
    }

    public boolean isVariglonnstilskudd() {
        return this == Tiltakstype.VARIG_LONNSTILSKUDD;
    }

    public boolean isSommerjobb() {
        return this == Tiltakstype.SOMMERJOBB;
    }

    public boolean skalBesluttes() {
        return this == Tiltakstype.SOMMERJOBB ||
            this == Tiltakstype.VARIG_LONNSTILSKUDD ||
            this == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD ||
            this == Tiltakstype.VTAO;
    }
}
