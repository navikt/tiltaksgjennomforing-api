package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KrrPersonKontaktinformasjonRequest {
    private List<String> personidenter;
}
