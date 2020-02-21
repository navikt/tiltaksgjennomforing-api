package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;


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

    public ArbeidsgiverOrganisasjon konverterTilDomeneObjekt(Tiltakstype tilgangstype) {
        return new ArbeidsgiverOrganisasjon(new BedriftNr(organizationNumber), name, tilgangstype);
    }
}
