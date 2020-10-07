package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Tiltakstype {
    ARBEIDSTRENING("ab0422"), MIDLERTIDIG_LONNSTILSKUDD("ab0336"), VARIG_LONNSTILSKUDD("ab0337"), MENTOR("ab0416");

    final String behandlingstema;

    Tiltakstype(String behandlingstema) {
        this.behandlingstema = behandlingstema;
    }

    public String getBehandlingstema() {
        return this.behandlingstema;
    }
}
