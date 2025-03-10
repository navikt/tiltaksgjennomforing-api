package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
public class PersondataServiceTest {
    private static final Fnr STRENGT_FORTROLIG_PERSON = new Fnr("16053900422");
    private static final Fnr STRENGT_FORTROLIG_UTLAND_PERSON = new Fnr("28033114267");
    private static final Fnr FORTROLIG_PERSON = new Fnr("26067114433");
    private static final Fnr UGRADERT_PERSON = new Fnr("00000000000");
    private static final Fnr UGRADERT_PERSON_TOM_RESPONSE = new Fnr("27030960020");
    private static final Fnr USPESIFISERT_GRADERT_PERSON = new Fnr("18076641842");
    private static final Fnr PERSON_FINNES_IKKE = new Fnr("24080687881");
    private static final Fnr PERSON_FOR_RESPONS_UTEN_DATA = new Fnr("23097010706");
    private static final Fnr DONALD_DUCK = new Fnr("00000000000");
    @Autowired
    private PersondataService persondataService;

    @Test
    public void hentGradering__returnerer_strengt_fortrolig_person() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(STRENGT_FORTROLIG_PERSON);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.STRENGT_FORTROLIG);
    }

    @Test
    public void hentGradering__returnerer_strengt_fortrolig_utland_person() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(STRENGT_FORTROLIG_UTLAND_PERSON);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.STRENGT_FORTROLIG_UTLAND);
    }

    @Test
    public void hentGradering__returnerer_fortrolig_person() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(FORTROLIG_PERSON);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.FORTROLIG);
    }

    @Test
    public void hentGradering__returnerer_ugradert_person() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(UGRADERT_PERSON);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.UGRADERT);
    }

    @Test
    public void hentGradering__returnerer_tom_gradering() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(USPESIFISERT_GRADERT_PERSON);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.UGRADERT);
    }

    @Test
    public void hentGradering__person_finnes_ikke_er_ok() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(PERSON_FINNES_IKKE);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.UGRADERT);
    }

    @Test
    public void hentGradering__returnerer_ugradert_tom_gradering() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(UGRADERT_PERSON_TOM_RESPONSE);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.UGRADERT);
    }

    @Test
    public void hentGradering__person_får_respons_uten_data() {
        Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(PERSON_FOR_RESPONS_UTEN_DATA);
        assertThat(diskresjonskode).isEqualTo(Diskresjonskode.UGRADERT);
    }

    @Test
    public void hentNavn__tomt_navn_hvis_person_ikke_finens() {
        assertThat(persondataService.hentNavn(PERSON_FINNES_IKKE)).isEqualTo(Navn.TOMT_NAVN);
    }

    @Test
    public void hentNavn__navn_hvis_person_finnes() {
        assertThat(persondataService.hentNavn(DONALD_DUCK)).isEqualTo(new Navn("Donald", null, "Duck"));
    }

    @Test
    public void erKode6__strengt_fortrolig() {
        assertThat(persondataService.hentDiskresjonskode(STRENGT_FORTROLIG_PERSON).erKode6()).isTrue();
    }

    @Test
    public void erKode6__strengt_fortrolig_utland() {
        assertThat(persondataService.hentDiskresjonskode(STRENGT_FORTROLIG_UTLAND_PERSON).erKode6()).isTrue();
    }

    @Test
    public void erKode6__fortrolig() {
        assertThat(persondataService.hentDiskresjonskode(FORTROLIG_PERSON).erKode6()).isFalse();
    }

    @Test
    public void erKode6__ugradert() {
        assertThat(persondataService.hentDiskresjonskode(UGRADERT_PERSON).erKode6()).isFalse();
    }

    @Test
    public void erKode6__ugradertTom() {
        assertThat(persondataService.hentDiskresjonskode(UGRADERT_PERSON_TOM_RESPONSE).erKode6()).isFalse();
    }

    @Test
    public void erKode6__uspesifisert_gradering() {
        assertThat(persondataService.hentDiskresjonskode(USPESIFISERT_GRADERT_PERSON).erKode6()).isFalse();
    }

    @Test
    public void erKode6_person_finnes_ikke_er_ok() {
        assertThat(persondataService.hentDiskresjonskode(PERSON_FINNES_IKKE).erKode6()).isFalse();
    }

    @Test
    public void henterGeoTilhørighet() {
        assertThat(persondataService.hentGeografiskTilknytning(DONALD_DUCK).get()).isEqualTo("030104");
    }

}
