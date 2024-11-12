package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

/**
 * En liste av relevante gosys-temakoder som vi benytter.
 * For fullstendig liste over temakoder,
 * <a href="https://kodeverk-web.intern.nav.no/kodeverksoversikt/kodeverk/Tema">se kodeverk</a>
 * (krever tilgang til sikker sone).
 */
enum GosysTema {
    TILTAK("TIL"), UFORETRYGD("UFO");

    private final String temakode;

    GosysTema(String temakode) {
        this.temakode = temakode;
    }

    public String getTemakode() {
        return this.temakode;
    }
}
