package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.EnhetFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.BrregProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class BrregService {
    private final BrregProperties brregProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public Organisasjon hentOrganisasjon(BedriftNr bedriftNr) {
        URI uri = UriComponentsBuilder.fromUri(brregProperties.getBrregUri())
                .pathSegment("enhetsregisteret", "enhet", bedriftNr.getBedriftNr() + ".json")
                .build()
                .toUri();
        try {
            return restTemplate.getForObject(uri, BrregEnhet.class).konverterTilDomeneObjekt();
        } catch (RestClientException e) {
            throw new EnhetFinnesIkkeException();
        }
    }
}
