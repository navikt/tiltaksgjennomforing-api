package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.FortroligAdresseException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.apache.commons.io.Charsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersondataService {
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final PersondataProperties persondataProperties;

    @Value("classpath:pdl/hentPerson.adressebeskyttelse.graphql")
    private Resource adressebeskyttelseQueryResource;

    @Value("classpath:pdl/hentPerson.navn.graphql")
    private Resource navnQueryResource;

    @Value("classpath:pdl/hentIdenter.graphql")
    private Resource identerQueryResource;

    @SneakyThrows
    private static String resourceAsString(Resource adressebeskyttelseQuery) {
        String filinnhold = StreamUtils.copyToString(adressebeskyttelseQuery.getInputStream(), Charsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

    protected Adressebeskyttelse hentAdressebeskyttelse(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(adressebeskyttelseQueryResource), new Variables(fnr.asString()));
        return hentAdressebeskyttelseFraPdlRespons(utførKallTilPdl(pdlRequest));
    }

    private HttpEntity<String> createRequestEntity(PdlRequest pdlRequest) {
        String stsToken = stsClient.hentSTSToken().getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tema", "GEN");
        headers.set("Nav-Consumer-Token", "Bearer " + stsToken);
        return new HttpEntity(pdlRequest, headers);
    }

    private static Adressebeskyttelse hentAdressebeskyttelseFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return pdlRespons.getData().getHentPerson().getAdressebeskyttelse()[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Adressebeskyttelse.INGEN_BESKYTTELSE;
        }
    }

    private static Navn hentNavnFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return pdlRespons.getData().getHentPerson().getNavn()[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Navn.TOMT_NAVN;
        }
    }

    private static String hentAktørIdFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return pdlRespons.getData().getHentIdenter().getIdenter()[0].getIdent();
        }catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    private PdlRespons utførKallTilPdl(PdlRequest pdlRequest) {
        try {
            return restTemplate.postForObject(persondataProperties.getUri(), createRequestEntity(pdlRequest), PdlRespons.class);
        } catch (RestClientException exception) {
            stsClient.evictToken();
            log.error("Feil fra PDL med request-url: " + persondataProperties.getUri(), exception);
            throw exception;
        }
    }

    public Navn hentNavn(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(navnQueryResource), new Variables(fnr.asString()));
        return hentNavnFraPdlRespons(utførKallTilPdl(pdlRequest));
    }

    public String hentAktørId(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(identerQueryResource), new Variables(fnr.asString()));
        return hentAktørIdFraPdlRespons(utførKallTilPdl(pdlRequest));
    }

    public void sjekkGradering(Fnr fnr) {
        String gradering = hentAdressebeskyttelse(fnr).getGradering();
        if ("FORTROLIG".equals(gradering) || "STRENGT_FORTROLIG".equals(gradering) || "STRENGT_FORTROLIG_UTLAND".equals(gradering)) {
            throw new FortroligAdresseException();
        }
    }
}
