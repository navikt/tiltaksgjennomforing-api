package no.nav.tag.tiltaksgjennomforing.integrasjon.axsys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AxsysEnhet {

    private String enhetId;

    public NavEnhet konverterTilDomeneObjekt() {
        return new NavEnhet(enhetId);
    }
}
