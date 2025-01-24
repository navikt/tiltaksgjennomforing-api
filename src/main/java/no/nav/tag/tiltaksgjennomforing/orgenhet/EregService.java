package no.nav.tag.tiltaksgjennomforing.orgenhet;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EregService {
    private List<String> arenaMigreringBedriftNr = List.of(
        "912006751",
        "921327072",
        "925586951",
        "925586129",
        "925076937"
    );

    private final EregProperties eregProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public Organisasjon hentVirksomhet(BedriftNr bedriftNr) {
        URI uri = UriComponentsBuilder.fromUri(eregProperties.getUri())
                .pathSegment(bedriftNr.asString())
                .build()
                .toUri();
        try {
            EregEnhet eregEnhet = restTemplate.getForObject(uri, EregEnhet.class);
            if ("JuridiskEnhet".equals(eregEnhet.type())) {
                throw new EnhetErJuridiskException();
            }
            if ("Organisasjonsledd".equals(eregEnhet.type())) {
                throw new EnhetErOrganisasjonsleddException();
            }
            if (!eregEnhet.erAktiv() && !arenaMigreringBedriftNr.contains(bedriftNr.asString())) {
                throw new EnhetErSlettetException();
            }
            return eregEnhet.konverterTilDomeneObjekt();
        } catch (RestClientException e) {
            throw new EnhetFinnesIkkeException();
        }
    }
}
