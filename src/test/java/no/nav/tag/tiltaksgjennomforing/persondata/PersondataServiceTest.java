package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.tag.tiltaksgjennomforing.Miljø;
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
@ActiveProfiles({ Miljø.LOCAL, "wiremock"})
@DirtiesContext
public class PersondataServiceTest {
    @Autowired
    private PersondataService persondataService;

    private static final Fnr STRENGT_FORTROLIG_PERSON = new Fnr("16053900422");
    private static final Fnr STRENGT_FORTROLIG_UTLAND_PERSON = new Fnr("28033114267");
    private static final Fnr FORTROLIG_PERSON = new Fnr("26067114433");
    private static final Fnr UGRADERT_PERSON = new Fnr("00000000000");
    private static final Fnr UGRADERT_PERSON_TOM_RESPONSE = new Fnr("27030960020");
    private static final Fnr USPESIFISERT_GRADERT_PERSON = new Fnr("18076641842");
    private static final Fnr PERSON_FINNES_IKKE = new Fnr("24080687881");
    private static final Fnr PERSON_FOR_RESPONS_UTEN_DATA = new Fnr("23097010706");
    private static final Fnr DONALD_DUCK = new Fnr("00000000000");

    @Test
    public void hentGradering__returnerer_strengt_fortrolig_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(STRENGT_FORTROLIG_PERSON);
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("STRENGT_FORTROLIG");
    }

    @Test
    public void hentGradering__returnerer_strengt_fortrolig_utland_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(STRENGT_FORTROLIG_UTLAND_PERSON);
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("STRENGT_FORTROLIG_UTLAND");
    }

    @Test
    public void hentGradering__returnerer_fortrolig_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(FORTROLIG_PERSON);
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("FORTROLIG");
    }

    @Test
    public void hentGradering__returnerer_ugradert_person() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(UGRADERT_PERSON);
        assertThat(adressebeskyttelse.getGradering()).isEqualTo("UGRADERT");
    }

    @Test
    public void hentGradering__returnerer_tom_gradering() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(USPESIFISERT_GRADERT_PERSON);
        assertThat(adressebeskyttelse).isEqualTo(Adressebeskyttelse.INGEN_BESKYTTELSE);
    }

    @Test
    public void hentGradering__person_finnes_ikke_er_ok() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(PERSON_FINNES_IKKE);
        assertThat(adressebeskyttelse).isEqualTo(Adressebeskyttelse.INGEN_BESKYTTELSE);
    }

    @Test
    public void hentGradering__returnerer_ugradert_tom_gradering() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(UGRADERT_PERSON_TOM_RESPONSE);
        assertThat(adressebeskyttelse).isEqualTo(Adressebeskyttelse.INGEN_BESKYTTELSE);
    }

    @Test
    public void hentGradering__person_får_respons_uten_data() {
        Adressebeskyttelse adressebeskyttelse = persondataService.hentAdressebeskyttelse(PERSON_FOR_RESPONS_UTEN_DATA);
        assertThat(adressebeskyttelse).isEqualTo(Adressebeskyttelse.INGEN_BESKYTTELSE);
    }

    @Test
    public void hentNavn__tomt_navn_hvis_person_ikke_finens() {
        Navn navn = persondataService.hentNavn(PERSON_FINNES_IKKE);
        assertThat(navn).isEqualTo(Navn.TOMT_NAVN);
    }

    @Test
    public void hentNavn__navn_hvis_person_finnes() {
        Navn navn = persondataService.hentNavn(DONALD_DUCK);
        assertThat(navn).isEqualTo(new Navn("Donald", null, "Duck"));
    }

    @Test
    public void erKode6__strengt_fortrolig() {
        assertThat(persondataService.erKode6(STRENGT_FORTROLIG_PERSON)).isTrue();
    }

    @Test
    public void erKode6__strengt_fortrolig_utland() {
        assertThat(persondataService.erKode6(STRENGT_FORTROLIG_UTLAND_PERSON)).isTrue();
    }

    @Test
    public void erKode6__fortrolig() {
        assertThat(persondataService.erKode6(FORTROLIG_PERSON)).isFalse();
    }

    @Test
    public void erKode6__ugradert() {
        assertThat(persondataService.erKode6(UGRADERT_PERSON)).isFalse();
    }

    @Test
    public void erKode6__ugradertTom() {
        assertThat(persondataService.erKode6(UGRADERT_PERSON_TOM_RESPONSE)).isFalse();
    }

    @Test
    public void erKode6__uspesifisert_gradering() {
        assertThat(persondataService.erKode6(USPESIFISERT_GRADERT_PERSON)).isFalse();
    }

    @Test
    public void erKode6_person_finnes_ikke_er_ok() {
        assertThat(persondataService.erKode6(PERSON_FINNES_IKKE)).isFalse();
    }

}
