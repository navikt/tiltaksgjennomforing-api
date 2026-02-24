package no.nav.tag.tiltaksgjennomforing.orgenhet;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EregService {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public EregService(EregProperties eregProperties, RestTemplateBuilder restTemplateBuilder) {
        this.baseUrl = eregProperties.getUri().toString();
        this.restTemplate = restTemplateBuilder.build();
    }

    public Organisasjon hentVirksomhet(BedriftNr bedriftNr) {
        var bedriftNrString = bedriftNr.asString();
        if (!bedriftNrString.matches("^\\d{9}$")) {
            throw new IllegalArgumentException("Ugyldig bedriftsnummer: " + bedriftNrString + ".");
        }
        try {
            EregEnhet eregEnhet = restTemplate.getForObject(
                baseUrl + "/{bedriftNr}",
                EregEnhet.class,
                Map.of("bedriftNr", bedriftNrString)
            );
            if ("JuridiskEnhet".equals(eregEnhet.type())) {
                throw new EnhetErJuridiskException();
            }
            if ("Organisasjonsledd".equals(eregEnhet.type())) {
                throw new EnhetErOrganisasjonsleddException();
            }
            if (!eregEnhet.erAktiv()) {
                throw new EnhetErSlettetException();
            }
            return eregEnhet.konverterTilDomeneObjekt();
        } catch (RestClientException e) {
            throw new EnhetFinnesIkkeException();
        }
    }
}
