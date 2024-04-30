package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

public record FnrOgBedrift(
        Fnr deltakerFnr,
        BedriftNr bedriftNr
) {
}
