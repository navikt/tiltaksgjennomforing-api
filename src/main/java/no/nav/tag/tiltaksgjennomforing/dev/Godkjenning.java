package no.nav.tag.tiltaksgjennomforing.dev;

import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

public record Godkjenning(
        NavIdent beslutterIdent,
        String kostnadssted
) {
}
