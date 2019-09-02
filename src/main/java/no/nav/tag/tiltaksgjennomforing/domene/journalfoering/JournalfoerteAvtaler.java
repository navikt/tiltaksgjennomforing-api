package no.nav.tag.tiltaksgjennomforing.domene.journalfoering;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalfoerteAvtaler {

    private Map<UUID, String> avtaleJournalpostId;
}
