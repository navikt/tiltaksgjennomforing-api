package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class PersondataService {
    private static final String BEHANDLINGSNUMMER = "B662";
    private static final Set<Diskresjonskode> KODE_6 = Set.of(
        Diskresjonskode.STRENGT_FORTROLIG,
        Diskresjonskode.STRENGT_FORTROLIG_UTLAND
    );
    private static final Set<Diskresjonskode> KODE_7 = Set.of(
        Diskresjonskode.FORTROLIG
    );

    private final RestTemplate azureRestTemplate;
    private final PersondataProperties persondataProperties;

    @Value("classpath:pdl/hentPerson.adressebeskyttelse.graphql")
    private Resource adressebeskyttelseQueryResource;

    @Value("classpath:pdl/hentPersondata.graphql")
    private Resource persondataQueryResource;

    @Value("classpath:pdl/hentIdenter.graphql")
    private Resource identerQueryResource;

    public PersondataService(
        RestTemplate azureRestTemplate,
        PersondataProperties persondataProperties
    ) {
        this.azureRestTemplate = azureRestTemplate;
        this.persondataProperties = persondataProperties;
    }

    @SneakyThrows
    private static String resourceAsString(Resource adressebeskyttelseQuery) {
        String filinnhold = StreamUtils.copyToString(adressebeskyttelseQuery.getInputStream(), StandardCharsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

    public Adressebeskyttelse hentAdressebeskyttelse(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(adressebeskyttelseQueryResource), new Variables(fnr.asString()));
        return hentAdressebeskyttelseFraPdlRespons(utførKallTilPdl(pdlRequest));
    }

    private HttpEntity<String> createRequestEntity(PdlRequest pdlRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Behandlingsnummer", BEHANDLINGSNUMMER);
        return new HttpEntity(pdlRequest, headers);
    }

    private static Adressebeskyttelse hentAdressebeskyttelseFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return pdlRespons.getData().getHentPerson().getAdressebeskyttelse()[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Adressebeskyttelse.INGEN_BESKYTTELSE;
        }
    }

    public static Navn hentNavnFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return pdlRespons.getData().getHentPerson().getNavn()[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Navn.TOMT_NAVN;
        }
    }

    private static String hentAktørIdFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return pdlRespons.getData().getHentIdenter().getIdenter()[0].getIdent();
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    public static Optional<String> hentGeoLokasjonFraPdlRespons(PdlRespons pdlRespons) {
        try {
            return Optional.of(pdlRespons.getData().getHentGeografiskTilknytning().getGeoTilknytning());
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    private PdlRespons utførKallTilPdl(PdlRequest pdlRequest) {
        try {
            return azureRestTemplate.postForObject(persondataProperties.getUri(), createRequestEntity(pdlRequest), PdlRespons.class);
        } catch (RestClientException exception) {
            log.error("Feil fra PDL med request-url: {}", persondataProperties.getUri(), exception);
            throw exception;
        }
    }

    public AktorId hentAktørId(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(identerQueryResource), new Variables(fnr.asString()));
        return AktorId.av(hentAktørIdFraPdlRespons(utførKallTilPdl(pdlRequest)));
    }

    public boolean erKode6Eller7(Fnr fnr) {
        Diskresjonskode gradering = hentAdressebeskyttelse(fnr).getGradering();
        return KODE_6.contains(gradering) || KODE_7.contains(gradering);
    }

    public boolean erKode6(PdlRespons pdlRespons) {
        try {
            Diskresjonskode gradering = hentAdressebeskyttelseFraPdlRespons(pdlRespons).getGradering();
            return KODE_6.contains(gradering);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Cacheable(CacheConfig.PDL_CACHE)
    public boolean erKode6(Fnr fnr) {
        Diskresjonskode gradering = hentAdressebeskyttelse(fnr).getGradering();
        return KODE_6.contains(gradering);
    }

    @Cacheable(CacheConfig.PDL_CACHE)
    public PdlRespons hentPersondataFraPdl(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(persondataQueryResource), new Variables(fnr.asString()));
        return utførKallTilPdl(pdlRequest);
    }

    public PdlRespons hentPersondata(Fnr fnr) {
        PdlRequest pdlRequest = new PdlRequest(resourceAsString(persondataQueryResource), new Variables(fnr.asString()));
        return utførKallTilPdl(pdlRequest);
    }
}
