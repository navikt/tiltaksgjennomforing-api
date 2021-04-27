package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dvh_melding")
public class DvhMeldingEntitet {
    @Id
    private UUID meldingId;
    private UUID avtaleId;
    private LocalDateTime tidspunkt;
    private String json;
}
