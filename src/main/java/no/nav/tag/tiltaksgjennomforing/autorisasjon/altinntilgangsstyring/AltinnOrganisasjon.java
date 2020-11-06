package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class AltinnOrganisasjon {
    String name;
    String type;
    String organizationNumber;
    String organizationForm;
    String status;
    String parentOrganizationNumber;
}
