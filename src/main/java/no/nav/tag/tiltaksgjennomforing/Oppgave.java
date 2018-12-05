package no.nav.tag.tiltaksgjennomforing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Oppgave {
    @Id
    private Integer id;
    private LocalDateTime opprettetTidspunkt = LocalDateTime.now();
    private String tittel;
    private String beskrivelse;
    private String opplaering;
    @JsonIgnore
    private Integer avtale;
}