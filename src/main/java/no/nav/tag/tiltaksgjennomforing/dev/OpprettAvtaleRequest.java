package no.nav.tag.tiltaksgjennomforing.dev;

import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreAvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;

record OpprettAvtaleRequest(
        OpprettAvtale opprett,
        EndreAvtaleDTO endre
) {
}
