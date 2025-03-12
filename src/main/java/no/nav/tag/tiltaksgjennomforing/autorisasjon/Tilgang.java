package no.nav.tag.tiltaksgjennomforing.autorisasjon;

public sealed interface Tilgang {
    record Tillat() implements Tilgang {}
    record Avvis(Avslagskode tilgangskode) implements Tilgang {}
}
