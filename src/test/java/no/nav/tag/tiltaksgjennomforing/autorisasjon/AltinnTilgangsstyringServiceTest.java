package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltinnFeilException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Map;
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
        Fnr fnr = new Fnr("10000000000");
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = altinnTilgangsstyringService.hentTilganger(fnr);
        Set<AltinnOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(fnr);

        // Alt som finnes i tilganger-mappet skal også finnes i organisasjoner-settet
        assertThat(organisasjoner).extracting(org -> new BedriftNr(org.getOrganizationNumber())).containsAll(tilganger.keySet());

        // Parents skal ikke være i tilgang-map
        assertThat(tilganger)
                .doesNotContainKey(new BedriftNr("910825550"))
                .doesNotContainKey(new BedriftNr("910825555"));

        // Virksomheter skal være i tilgang-map
        assertThat(tilganger.get(new BedriftNr("999999999"))).containsOnly(Tiltakstype.ARBEIDSTRENING, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        assertThat(tilganger.get(new BedriftNr("910712314"))).containsOnly(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        assertThat(tilganger.get(new BedriftNr("910712306"))).containsOnly(Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__tilgang_bare_for_arbeidstrening() {
        Fnr fnr = new Fnr("20000000000");
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = altinnTilgangsstyringService.hentTilganger(fnr);
        Set<AltinnOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(fnr);

        // Alt som finnes i tilganger-mappet skal også finnes i organisasjoner-settet
        assertThat(organisasjoner).extracting(org -> new BedriftNr(org.getOrganizationNumber())).containsAll(tilganger.keySet());

        // Parents skal ikke være i tilgang-map
        assertThat(tilganger).doesNotContainKey(new BedriftNr("910825555"));

        // Virksomheter skal være i tilgang-map
        assertThat(tilganger.get(new BedriftNr("999999999"))).containsOnly(Tiltakstype.ARBEIDSTRENING);
    }

    @Test
    public void hentOrganisasjoner__ingen_tilgang() {
        Fnr fnr = new Fnr("09000000000");
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = altinnTilgangsstyringService.hentTilganger(fnr);
        Set<AltinnOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(fnr);

        assertThat(organisasjoner).isEmpty();
        assertThat(tilganger).isEmpty();
    }

    @Test(expected = AltinnFeilException.class)
    public void hentTilganger__midlertidig_feil_gir_feilkode() {
        altinnTilgangsstyringService.hentTilganger(new Fnr("31000000000"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void manglende_serviceCode_skal_kaste_feil() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        new AltinnTilgangsstyringService(altinnTilgangsstyringProperties, tokenUtils, featureToggleService);
    }
}
