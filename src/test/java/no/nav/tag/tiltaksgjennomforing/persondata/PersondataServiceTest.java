package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
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

    private Fnr strengtFortroligPerson = new Fnr("16053900422");
    private Fnr fortroligPerson = new Fnr("26067114433");
    private Fnr ugradertPErson = new Fnr("00000000000");
    private Fnr uspesifisertGradertPerson = new Fnr("18076641842");
    private Fnr personFinnesIkke = new Fnr("24080687881");
    private Fnr personForResponsUtenData = new Fnr("23097010706");

    @Test
    public void hentGradering__returnerer_strengt_fortrolig_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(strengtFortroligPerson);
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("STRENGT_FORTROLIG");
    }

    @Test
    public void hentGradering__returnerer_fortrolig_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(fortroligPerson);
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("FORTROLIG");
    }

    @Test
    public void hentGradering__returnerer_ugradert_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(new Fnr("00000000000"));
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("UGRADERT");
    }

    @Test
    public void hentGradering__returnerer_tom_gradering() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(new Fnr("18076641842"));
        assertThat(adressebeskyttelse.getGradering()).isBlank();
    }

    @Test
    public void hentGradering__person_finnes_ikke_er_ok() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(personFinnesIkke);
        assertThat(adressebeskyttelse.getGradering().equals("null"));
    }

    @Test(expected = NullPointerException.class)
    public void hentGradering_person_far_respons_uten_Data() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentGradering(personForResponsUtenData);
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkGradering__skal_kaste_feile__strengt_fortrolig() { persondataService.sjekkGradering(strengtFortroligPerson); }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkGradering__skal_kaste_feile__fortrolig() {
        persondataService.sjekkGradering(fortroligPerson);
    }

    @Test
    public void sjekkGradering__skal_ikke_kaste_feil_ugradert() {
        persondataService.sjekkGradering(ugradertPErson);
    }

    @Test
    public void sjekkGradering__skal_ikke_kaste_feil_uspesifisert_gradering() { persondataService.sjekkGradering(uspesifisertGradertPerson); }

    @Test
    public void sjekkGradering_person_finnes_ikke_er_ok() {
        persondataService.sjekkGradering(personFinnesIkke);
    }

}
