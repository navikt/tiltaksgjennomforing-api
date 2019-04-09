package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.EnhetFinnesIkkeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"dev", "mock"})
@DirtiesContext
public class BrregServiceTest {
    @Autowired
    private BrregService brregService;

    @Test
    public void hentBedriftNavn__returnerer_navn_og_bedriftnr() {
        Organisasjon organisasjon = brregService.hentOrganisasjon(new BedriftNr("899999999"));
        assertThat(organisasjon.getBedriftNr()).isEqualTo(new BedriftNr("899999999"));
        assertThat(organisasjon.getNavn()).isEqualTo("BEDRIFTEN AS");
    }

    @Test(expected = EnhetFinnesIkkeException.class)
    public void hentBedriftNavn__kaster_exception_ved_404() {
        brregService.hentOrganisasjon(new BedriftNr("999999999"));
    }
}