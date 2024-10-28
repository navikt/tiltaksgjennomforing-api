package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

/**
 * En liste av relevante gosys-behandlingstyper som vi benytter.
 * For fullstendig liste over behandlingstyper,
 * <a href="https://kodeverk-web.intern.nav.no/kodeverksoversikt/kodeverk/Behandlingstyper">se kodeverk</a>
 * (krever tilgang til sikker sone).
 */
public enum GosysBehandlingstype {
    SOKNAD("ae0034"), INGEN(null);

    private final String behandlingstypekode;

    GosysBehandlingstype(String behandlingstypekode) {
        this.behandlingstypekode = behandlingstypekode;
    }

    public String getBehandlingstypekode() {
        return this.behandlingstypekode;
    }
}
