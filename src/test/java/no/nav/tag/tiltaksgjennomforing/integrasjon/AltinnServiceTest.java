package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.AltinnException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"mock", "dev"})
@DirtiesContext
public class AltinnServiceTest {
    @Autowired
    private AltinnService altinnService;

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_bedrift() {
        List<Organisasjon> organisasjoner = altinnService.hentOrganisasjoner(new Fnr("10000000000"));
        assertThat(organisasjoner).extracting("bedriftNr").containsOnly(new BedriftNr("111111111"));
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_tom_liste() {
        List<Organisasjon> organisasjoner = altinnService.hentOrganisasjoner(new Fnr("00000000000"));
        assertThat(organisasjoner).hasSize(0);
    }

    @Test(expected = AltinnException.class)
    public void hentOrganisasjoner__ugyldig_fnr_skal_gi_feil() {
        altinnService.hentOrganisasjoner(TestData.enIdentifikator());
    }
}