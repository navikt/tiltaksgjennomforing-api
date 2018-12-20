package no.nav.tag.tiltaksgjennomforing.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;

@Data
@AllArgsConstructor
public class OpprettAvtaleRequest {
    private Fnr deltakerFnr;
    private NavIdent veilederNavIdent;

    public Avtale create() {
        return Avtale.nyAvtale(deltakerFnr, veilederNavIdent);
    }
}
