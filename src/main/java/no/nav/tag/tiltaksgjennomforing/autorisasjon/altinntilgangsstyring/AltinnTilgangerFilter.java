package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import java.util.Set;

// https://arbeidsgiver-altinn-tilganger.intern.dev.nav.no/swagger-ui
public record AltinnTilgangerFilter(
        Set<String> altinn2Tilganger,
        Set<String> altinn3Tilganger,
        boolean inkluderSlettede
) {
    public AltinnTilgangerFilter(Set<String> altinn2Tilganger, Set<String> altinn3Tilganger) {
        this(altinn2Tilganger, altinn3Tilganger, true);
    }
}
