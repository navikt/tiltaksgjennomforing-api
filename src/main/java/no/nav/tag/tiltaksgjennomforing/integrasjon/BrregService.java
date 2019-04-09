package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.EnhetFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.BrregProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class BrregService {
    private final BrregProperties brregProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public String hentBedriftNavn(BedriftNr bedriftNr) {
        String uri = UriComponentsBuilder.fromUri(brregProperties.getBrregUri())
                .pathSegment("enhetsregisteret", "enhet", bedriftNr.getBedriftNr() + ".json")
                .build()
                .toUriString();
        try {
            return restTemplate.getForObject(uri, BrregEnhet.class).getNavn();
        } catch (RestClientException e) {
            throw new EnhetFinnesIkkeException();
        }
    }
}
