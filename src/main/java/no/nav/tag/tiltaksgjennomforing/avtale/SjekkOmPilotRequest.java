package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SjekkOmPilotRequest {
    private Fnr deltakerFnr;
    private BedriftNr bedriftNr;
    private Tiltakstype tiltakstype;
}
