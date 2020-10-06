package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Tiltakstype {
    ARBEIDSTRENING("ab0422"), MIDLERTIDIG_LONNSTILSKUDD("ab0422"), VARIG_LONNSTILSKUDD("ab0422"), MENTOR("ab0422");

    final String behandlingstema;

    Tiltakstype(String behandlingstema) {
        this.behandlingstema = behandlingstema;
    }

    public String getBehandlingstema() {
        return this.behandlingstema;
    }
}
