package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VarslbarHendelse {
    private LocalDateTime tidspunkt;
    private UUID avtaleId;
    private String hendelse;
    private List<Varsel> varsler;
}
