package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_tilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AltinnTilgangsstyringProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"dev", "wiremock"})
@DirtiesContext
public class AltinnTilgangsstyringServiceTest {
    @Autowired
    private AltinnTilgangsstyringService altinnTilgangsstyringService;

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_forste_bedrift() {
        List<Organisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("10000000000"));
        assertThat(organisasjoner).extracting("bedriftNr").containsOnly(new BedriftNr("999999999"));
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_andre_bedrift() {
        List<Organisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("20000000000"));
        assertThat(organisasjoner).extracting("bedriftNr").containsOnly(new BedriftNr("910909088"));
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_tom_liste() {
        List<Organisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("00000000000"));
        assertThat(organisasjoner).hasSize(0);
    }

    @Test
    public void hentOrganisasjoner__ugyldig_fnr_tom_liste() {
        List<Organisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(TestData.enIdentifikator());
        assertThat(organisasjoner).hasSize(0);
    }

    @Test
    public void hentOrganisasjoner__feilkonfigurasjon_tom_liste() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        altinnTilgangsstyringProperties.setUri(URI.create("http://foobar"));
        List<Organisasjon> organisasjoner = new AltinnTilgangsstyringService(altinnTilgangsstyringProperties).hentOrganisasjoner(TestData.enIdentifikator());
        assertThat(organisasjoner).hasSize(0);
    }
}