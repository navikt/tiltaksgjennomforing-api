package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

@Data
@AllArgsConstructor
public class KontaktinformasjonEndret {
    Avtale avtale;
}
