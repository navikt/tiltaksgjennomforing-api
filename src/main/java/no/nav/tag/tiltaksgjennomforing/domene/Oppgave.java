package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Oppgave {
    @Id
    private Integer id;
    private LocalDateTime opprettetTidspunkt;
    private String tittel;
    private String beskrivelse;
    private String opplaering;

    public void setterOppretterTidspunkt() {
        if (opprettetTidspunkt == null) {
            opprettetTidspunkt = LocalDateTime.now();
        }
    }
}