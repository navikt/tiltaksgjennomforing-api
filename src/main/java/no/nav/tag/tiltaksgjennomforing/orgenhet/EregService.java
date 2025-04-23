package no.nav.tag.tiltaksgjennomforing.orgenhet;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EregService {
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public EregService(EregProperties eregProperties) {
        this.baseUrl = eregProperties.getUri().toString();
    }

    public Organisasjon hentVirksomhet(BedriftNr bedriftNr) {
        try {
            EregEnhet eregEnhet = restTemplate.getForObject(
                baseUrl + "/{bedriftNr}",
                EregEnhet.class,
                Map.of("bedriftNr", bedriftNr.asString())
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
