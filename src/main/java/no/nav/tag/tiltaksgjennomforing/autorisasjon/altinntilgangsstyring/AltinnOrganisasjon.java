package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AltinnOrganisasjon {
    String name;
    String type;
    String organizationNumber;
    String organizationForm;
    String status;
    String parentOrganizationNumber;
}
