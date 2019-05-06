package no.nav.tag.tiltaksgjennomforing.integrasjon.ereg;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.EnhetErIkkeVirksomhetException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.EnhetFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.EregProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class EregService {
    private final EregProperties eregProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public Organisasjon hentVirksomhet(BedriftNr bedriftNr) {
        URI uri = UriComponentsBuilder.fromUri(eregProperties.getEregUri())
                .pathSegment("ereg", "api", "v1", "organisasjon", bedriftNr.getBedriftNr())
                .build()
                .toUri();
        try {
            EregEnhet eregEnhet = restTemplate.getForObject(uri, EregEnhet.class);
            if (!"Virksomhet".equals(eregEnhet.getType())) {
                throw new EnhetErIkkeVirksomhetException();
            }
            return eregEnhet.konverterTilDomeneObjekt();
        } catch (RestClientException e) {
            throw new EnhetFinnesIkkeException();
        }
    }
}
