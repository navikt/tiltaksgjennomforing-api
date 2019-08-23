package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Oppgave implements IdOgTidspunktGenerator{
    @Id
    private UUID id;
    private LocalDateTime opprettetTidspunkt;
    private String tittel;
    private String beskrivelse;
    private String opplaering;

    @Override
    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (opprettetTidspunkt == null) {
            opprettetTidspunkt = LocalDateTime.now();
        }
    }
}