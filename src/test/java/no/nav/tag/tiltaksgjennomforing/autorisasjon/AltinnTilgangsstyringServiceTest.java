package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL, "wiremock" })
@DirtiesContext
public class AltinnTilgangsstyringServiceTest {
    @Autowired
    private AltinnTilgangsstyringService altinnTilgangsstyringService;

    @MockBean
    private TokenUtils tokenUtils;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        when(tokenUtils.hentSelvbetjeningToken()).thenReturn("token");
        when(featureToggleService.isEnabled(anyString())).thenReturn(false);
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_bedrift_på_hvert_tiltak() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("10000000000"));
        assertThat(organisasjoner).extracting(ArbeidsgiverOrganisasjon.Fields.bedriftNr).containsOnly(new BedriftNr("999999999"), new BedriftNr("910712314"), new BedriftNr("910712306"), new BedriftNr("910825550"));
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_på_arbeidstrening_og_varig() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("20000000000"));
        assertThat(organisasjoner).flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper).containsOnly(Tiltakstype.ARBEIDSTRENING, Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_på_midlertidig_lonnstilskudd() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("26000000000"));
        assertThat(organisasjoner).flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper).containsOnly(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_på_varig_lonnstilskudd() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("27000000000"));
        assertThat(organisasjoner).flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper).containsOnly(Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_på_alle_tiltakstyper() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("10000000000"));
        assertThat(organisasjoner).flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper).contains(Tiltakstype.ARBEIDSTRENING, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_andre_bedrift() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("20000000000"));
        assertThat(organisasjoner).extracting("bedriftNr").containsOnly(new BedriftNr("981121465"), new BedriftNr("910909088"), new BedriftNr("910825550"), new BedriftNr("910825552"));
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_tom_liste() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr("00000000000"));
        assertThat(organisasjoner).hasSize(0);
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__ugyldig_fnr_skal_kaste_feil() {
        altinnTilgangsstyringService.hentOrganisasjoner(TestData.enIdentifikator());
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__feilkonfigurasjon_skal_kaste_feil() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        altinnTilgangsstyringProperties.setUri(URI.create("http://foobar"));
        new AltinnTilgangsstyringService(altinnTilgangsstyringProperties, tokenUtils, featureToggleService).hentOrganisasjoner(TestData.enIdentifikator());
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__manglende_serviceCode_skal_kaste_feil() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        new AltinnTilgangsstyringService(altinnTilgangsstyringProperties, tokenUtils, featureToggleService);
    }

    @Test
    public void hentOrganisasjoner__skal_ha_parentOrganization() {
        Set<AltinnOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(new Fnr("27999999999"));
        AltinnOrganisasjon organisasjon = organisasjoner.stream().filter(org -> org.getOrganizationNumber().equals("910825607")).findFirst().orElseThrow();

        assertThat(organisasjon.getOrganizationForm()).isEqualTo("BEDR");
        assertThat(organisasjon.getStatus()).isEqualTo("Active");
        assertThat(organisasjon.getType()).isEqualTo("Business");
        assertThat(organisasjon.getName()).isEqualTo("BIRTAVARRE OG VÆRLANDET REGNSKAP");
        assertThat(organisasjon.getParentOrganizationNumber()).isEqualTo("910825550");
    }

    @Test
    public void hentAltinnOrganisasjoner__skal_kun_ha_en_org_selvom_flere_tilganger() {
        Set<AltinnOrganisasjon> altinnOrganisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(new Fnr("30000000000"));
        assertThat(altinnOrganisasjoner).filteredOn(a -> a.getOrganizationNumber().equals("910825607")).hasSize(1);
    }

}
