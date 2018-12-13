package no.nav.tag.tiltaksgjennomforing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class Maal {
    @Id
    private final  Integer id;
    private final LocalDateTime opprettetTidspunkt;
    private String kategori;
    private String beskrivelse;
    @JsonIgnore
    private Integer avtale;
}
