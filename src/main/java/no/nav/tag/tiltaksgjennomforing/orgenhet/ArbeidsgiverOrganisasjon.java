package no.nav.tag.tiltaksgjennomforing.orgenhet;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.util.ArrayList;
import java.util.List;

@Value
public class ArbeidsgiverOrganisasjon {
    private final BedriftNr bedriftNr;
    private final String bedriftNavn;
    private final List<Tiltakstype> tilgangstyper = new ArrayList<>();
}