package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Maal {
    @Id
    private Integer id;
    private LocalDateTime opprettetTidspunkt;
    private String kategori;
    private String beskrivelse;

    public void setterOppretterTidspunkt() {
        if (opprettetTidspunkt == null) {
            opprettetTidspunkt = LocalDateTime.now();
        }
    }
}
