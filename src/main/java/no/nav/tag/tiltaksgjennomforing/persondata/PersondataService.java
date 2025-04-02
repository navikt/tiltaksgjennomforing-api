package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import no.nav.team_tiltak.felles.persondata.PersondataClient;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersondataService {
    private final PersondataClient persondataClient;

    public PersondataService(
        PersondataProperties persondataProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService,
        ClientConfigurationProperties clientConfigurationProperties
    ) {
        ClientProperties clientProperties = clientConfigurationProperties.getRegistration().get("pdl-api");

        this.persondataClient = new PersondataClient(
            persondataProperties.getUri(),
            () -> Optional.ofNullable(clientProperties)
                .map(prop -> oAuth2AccessTokenService.getAccessToken(prop).getAccessToken())
                .orElse(null)
        );
    }

    public Diskresjonskode hentDiskresjonskode(Fnr fnr) {
        return persondataClient.hentDiskresjonskode(fnr.asString()).orElse(Diskresjonskode.UGRADERT);
    }

    public Map<Fnr, Diskresjonskode> hentDiskresjonskoder(Set<Fnr> fnrSet) {
        return persondataClient.hentDiskresjonskoderEllerDefault(
            fnrSet.stream().map(Fnr::asString).collect(Collectors.toSet()),
            Fnr::av,
            Diskresjonskode.UGRADERT
        );
    }

    public Optional<AktorId> hentGjeldendeAktørId(Fnr fnr) {
        return persondataClient.hentGjeldendeAktorId(fnr.asString()).map(AktorId::av);
    }

    public Optional<String> hentGeografiskTilknytning(Fnr fnr) {
        return persondataClient.hentGeografiskTilknytning(fnr.asString());
    }

    public Navn hentNavn(Fnr fnr) {
        return persondataClient.hentNavn(fnr.asString()).orElse(Navn.TOMT_NAVN);
    }

}
