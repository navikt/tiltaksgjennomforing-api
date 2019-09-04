package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;

public interface VarselService {
    void sendVarsel(Identifikator avgiver, String telefonnummer, String varseltekst);
}
