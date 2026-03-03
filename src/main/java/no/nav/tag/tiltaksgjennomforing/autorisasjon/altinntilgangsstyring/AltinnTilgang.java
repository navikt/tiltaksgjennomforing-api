package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import java.util.List;
import java.util.Set;

public record AltinnTilgang(
        String orgnr,
        Set<String> altinn3Tilganger,
        Set<String> altinn2Tilganger,
        List<AltinnTilgang> underenheter,
        String navn,
        String organisasjonsform,
        boolean erSlettet
) {}
