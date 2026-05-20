package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KrrPersonKontaktinformasjonRespons {
    private Map<String, Kontaktinfo> personer;
}
