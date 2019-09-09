package no.nav.tag.tiltaksgjennomforing.domene.journalfoering;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GodkjentPaVegneGrunnTilJournalfoering {

    private boolean ikkeBankId;
    private boolean digitalKompetanse;
    private boolean reservert;
}
