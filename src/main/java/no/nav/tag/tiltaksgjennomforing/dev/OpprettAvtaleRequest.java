package no.nav.tag.tiltaksgjennomforing.dev;

import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;

record OpprettAvtaleRequest(
        OpprettAvtale opprett,
        EndreAvtale endre
) {
}
