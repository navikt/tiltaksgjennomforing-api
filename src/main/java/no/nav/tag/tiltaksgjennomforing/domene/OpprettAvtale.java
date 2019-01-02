package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpprettAvtale {
    private Fnr deltakerFnr;
    private NavIdent veilederNavIdent;
}
