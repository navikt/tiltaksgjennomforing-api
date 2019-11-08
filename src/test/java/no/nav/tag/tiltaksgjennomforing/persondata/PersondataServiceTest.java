package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
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
@ActiveProfiles({"dev", "wiremock"})
@DirtiesContext
public class PersondataServiceTest {
    @Autowired
    private PersondataService persondataService;

    @Test
    public void hentGradering__returnerer_strengt_fortrolig_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(new Fnr("16053900422"));
        assertThat(adressebeskyttelse.gradering).isEqualTo("STRENGT_FORTROLIG");
    }

    @Test
    public void hentGradering__returnerer_fortrolig_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(new Fnr("26067114433"));
        assertThat(adressebeskyttelse.gradering).isEqualTo("FORTROLIG");
    }

    @Test
    public void hentGradering__returnerer_ugradert_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(new Fnr("00000000000"));
        assertThat(adressebeskyttelse.gradering).isEqualTo("UGRADERT");
    }

    @Test
    public void hentGradering__returnerer_tom_gradering() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(new Fnr("18076641842"));
        assertThat(adressebeskyttelse.gradering).isBlank();
    }


}
