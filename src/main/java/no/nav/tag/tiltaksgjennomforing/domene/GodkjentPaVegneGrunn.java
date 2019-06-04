package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;


@Data
public class GodkjentPaVegneGrunn {
    @Id
    private UUID avtale;
    private boolean ikkeMinId;
    private boolean reservert;
    private boolean digitalKompetanse;
}