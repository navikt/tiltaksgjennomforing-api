package no.nav.tag.tiltaksgjennomforing.integrasjon.axsys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavEnhet;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AxsysEnhet {

    private String enhetId;

    public NavEnhet konverterTilDomeneObjekt() {
        return new NavEnhet(enhetId);
    }
}
