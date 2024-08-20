package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpprettAvtale {
    private Fnr deltakerFnr;
    private BedriftNr bedriftNr;
    private Tiltakstype tiltakstype;
}
