package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;

public interface VarselService {
    void sendVarsel(Identifikator avgiver, String telefonnummer, String varseltekst);
}
