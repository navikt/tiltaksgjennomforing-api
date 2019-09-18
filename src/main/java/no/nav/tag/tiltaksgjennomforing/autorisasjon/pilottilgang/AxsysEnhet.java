package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AxsysEnhet {
    private String enhetId;
}