package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import java.util.List;
import java.util.Map;
import java.util.Set;

// https://arbeidsgiver-altinn-tilganger.intern.dev.nav.no/swagger-ui
public record AltinnTilgangerResponse(
        boolean isError,
        List<AltinnTilgang> hierarki,
        Map<String, Set<String>> orgNrTilTilganger,
        Map<String, Set<String>> tilgangTilOrgNr
) {}
