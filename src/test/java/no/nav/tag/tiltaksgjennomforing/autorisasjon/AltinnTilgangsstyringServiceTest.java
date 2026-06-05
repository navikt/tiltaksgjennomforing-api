package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangerDto;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
public class AltinnTilgangsstyringServiceTest {
    private Fnr fnr;

    @Autowired
    private AltinnTilgangsstyringService altinnTilgangsstyringService;

    @MockBean
    private TokenUtils tokenUtils;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        when(featureToggleService.isEnabled(anyString())).thenReturn(false);
        fnr = Fnr.generer(25);
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_bedrift_på_hvert_tiltak() {
        AltinnTilgangerDto dto = altinnTilgangsstyringService.hentAltinnTilganger(fnr);
        Map<BedriftNr, Set<Tiltakstype>> tilganger = dto.tilganger();

        // Parents skal ikke være i tilgang-map
        assertThat(tilganger).doesNotContainKeys(new BedriftNr("910825550"), new BedriftNr("910825555"));

        // Virksomheter skal være i tilgang-map med forventede tiltakstyper
        assertThat(tilganger.get(new BedriftNr("999999999"))).containsOnly(Tiltakstype.ARBEIDSTRENING, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.SOMMERJOBB, Tiltakstype.MENTOR, Tiltakstype.INKLUDERINGSTILSKUDD, Tiltakstype.VTAO);
        assertThat(tilganger.get(new BedriftNr("910712314"))).containsOnly(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        assertThat(tilganger.get(new BedriftNr("910712306"))).containsOnly(Tiltakstype.VARIG_LONNSTILSKUDD);

        // Ingen tilganger på ingen tiltak
        assertThat(tilganger).doesNotContainKeys(new BedriftNr("980712306"), new BedriftNr("980825560"));
    }

    @Test
    public void hentOrganisasjoner__tilgang_bare_for_arbeidstrening() {
        AltinnTilgangerDto dto = altinnTilgangsstyringService.hentAltinnTilganger(fnr);
        Map<BedriftNr, Set<Tiltakstype>> tilganger = dto.tilganger();

        // Parents skal ikke være i tilgang-map
        assertThat(tilganger).doesNotContainKey(new BedriftNr("910825555"));

        // Virksomheter skal være i tilgang-map
        assertThat(tilganger.get(new BedriftNr("999999999"))).containsOnly(
                Tiltakstype.ARBEIDSTRENING, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD,
                Tiltakstype.SOMMERJOBB, Tiltakstype.MENTOR, Tiltakstype.INKLUDERINGSTILSKUDD, Tiltakstype.VTAO); // TODO: Tilgangsstyring skal skille på midlertidig lønnstilskudd og sommerjobb
    }

    @Test
    public void manglende_serviceCode_skal_kaste_feil() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        assertThatThrownBy(() -> new AltinnTilgangsstyringService(altinnTilgangsstyringProperties, null)).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void hentAltinn3__Organisasjoner__returnerer_hierarki_og_tilgangsmappinger() {
        AltinnTilgangerDto dto = altinnTilgangsstyringService.hentAltinnTilganger(fnr);

        assertThat(dto).isNotNull();

        // Sjekk hierarki
        assertThat(dto.hierarki()).isNotEmpty();
        assertThat(dto.hierarki()).extracting(h -> h.orgnr()).contains("910825555", "910825550");

        // Sjekk tilganger
        assertThat(dto.tilganger()).containsKey(new BedriftNr("999999999"));
        assertThat(dto.tilganger().get(new BedriftNr("999999999"))).containsOnly(
            Tiltakstype.ARBEIDSTRENING, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB, Tiltakstype.MENTOR, Tiltakstype.INKLUDERINGSTILSKUDD, Tiltakstype.VTAO
        );
        assertThat(dto.tilganger().get(new BedriftNr("910712314"))).containsOnly(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        assertThat(dto.tilganger().get(new BedriftNr("910712306"))).containsOnly(Tiltakstype.VARIG_LONNSTILSKUDD);

        // Bedrift uten tilganger (IngenTiltak Hjørnet) skal ikke være i tilganger
        assertThat(dto.tilganger()).doesNotContainKey(new BedriftNr("980712306"));
    }

    @Test
    public void mapTilgangerFraAltinn3__returnerer_tilganger_per_bedrift() {
        AltinnTilgangerDto dto = altinnTilgangsstyringService.hentAltinnTilganger(fnr);
        Map<BedriftNr, Set<Tiltakstype>> tilganger = dto.tilganger();

        assertThat(tilganger).isNotEmpty();

        // Parents skal ikke være i tilgang-map
        assertThat(tilganger).doesNotContainKeys(new BedriftNr("910825550"), new BedriftNr("910825555"));

        // 999999999 har alle tilganger
        assertThat(tilganger.get(new BedriftNr("999999999"))).containsOnly(
            Tiltakstype.ARBEIDSTRENING, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB, Tiltakstype.MENTOR, Tiltakstype.INKLUDERINGSTILSKUDD, Tiltakstype.VTAO
        );

        // 910712314 har bare midlertidig lønnstilskudd
        assertThat(tilganger.get(new BedriftNr("910712314"))).containsOnly(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        // 910712306 har bare varig lønnstilskudd
        assertThat(tilganger.get(new BedriftNr("910712306"))).containsOnly(Tiltakstype.VARIG_LONNSTILSKUDD);

        // Bedrift uten tilganger skal ikke være med
        assertThat(tilganger).doesNotContainKey(new BedriftNr("980712306"));
    }
}
