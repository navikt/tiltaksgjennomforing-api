package no.nav.tag.tiltaksgjennomforing.journalfoering;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InkluderingstilskuddsutgiftTilJournalfoering {
    private UUID id;
    private String navn;
    private Integer beløp;
    private String beskrivelse;
    private InkluderingstilskuddsutgiftType type;
}
