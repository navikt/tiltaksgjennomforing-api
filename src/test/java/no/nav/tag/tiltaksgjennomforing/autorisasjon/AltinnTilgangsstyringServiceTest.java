package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
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
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("10000000000"));
        assertThat(organisasjoner).extracting("bedriftNr").containsOnly(new BedriftNr("999999999"));
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_andre_bedrift() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("20000000000"));
        assertThat(organisasjoner).extracting("bedriftNr").containsOnly(new BedriftNr("981121465"), new BedriftNr("910909088"));
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_tom_liste() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("00000000000"));
        assertThat(organisasjoner).hasSize(0);
    }

    @Test (expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__ugyldig_fnr_skal_kaste_feil() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(TestData.enIdentifikator());
    }

    @Test (expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__feilkonfigurasjon_skal_kaste_feil() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        altinnTilgangsstyringProperties.setUri(URI.create("http://foobar"));
        List<ArbeidsgiverOrganisasjon> organisasjoner = new AltinnTilgangsstyringService(altinnTilgangsstyringProperties).hentOrganisasjoner(TestData.enIdentifikator());
    }
}