package no.nav.tag.tiltaksgjennomforing.integrasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AltinnOrganisasjon {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("OrganizationNumber")
    private String organizationNumber;
    @JsonProperty("OrganizationForm")
    private String organizationForm;
    @JsonProperty("Status")
    private String status;

    public Organisasjon konverterTilDomeneObjekt() {
        return new Organisasjon(new BedriftNr(organizationNumber), name);
    }
}
