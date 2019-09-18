package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpprettAvtale {
    private Fnr deltakerFnr;
    private BedriftNr bedriftNr;
}
