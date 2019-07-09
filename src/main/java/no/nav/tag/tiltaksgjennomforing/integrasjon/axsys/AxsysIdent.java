package no.nav.tag.tiltaksgjennomforing.integrasjon.axsys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AxsysIdent {

    @JsonProperty("appIdent")
    private String appIdent;

    public NavIdent konverterTilDomeneObjekt() {
        return new NavIdent(appIdent);
    }
}
