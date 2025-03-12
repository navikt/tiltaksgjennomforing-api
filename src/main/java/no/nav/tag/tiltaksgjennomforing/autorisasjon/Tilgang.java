package no.nav.tag.tiltaksgjennomforing.autorisasjon;

public sealed interface Tilgang {
    boolean erTillat();

    record Tillat() implements Tilgang {
        @Override
        public boolean erTillat() {
            return true;
        }
    }
    record Avvis(Avslagskode tilgangskode, String melding) implements Tilgang {
        @Override
        public boolean erTillat() {
            return false;
        }
    }
}
