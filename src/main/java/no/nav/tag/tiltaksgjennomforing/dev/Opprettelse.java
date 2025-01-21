package no.nav.tag.tiltaksgjennomforing.dev;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;

@Data
public class Opprettelse {
    OpprettAvtale opprett;
    EndreAvtale endre;
}
