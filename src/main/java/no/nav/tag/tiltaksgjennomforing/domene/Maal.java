package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Maal {
    @Id
    private String id;
    private LocalDateTime opprettetTidspunkt;
    private String kategori;
    private String beskrivelse;

    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (opprettetTidspunkt == null) {
            opprettetTidspunkt = LocalDateTime.now();
        }
    }
}
