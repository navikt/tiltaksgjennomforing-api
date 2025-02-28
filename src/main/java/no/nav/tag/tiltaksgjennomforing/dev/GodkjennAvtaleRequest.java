package no.nav.tag.tiltaksgjennomforing.dev;

import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

record GodkjennAvtaleRequest(
        NavIdent beslutterIdent,
        String kostnadssted
) {
}
