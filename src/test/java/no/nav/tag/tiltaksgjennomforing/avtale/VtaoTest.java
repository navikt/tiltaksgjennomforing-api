package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class VtaoTest {
    @MockBean
    private InnloggingService innloggingService;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private PersondataService persondataService;
    @Mock
    private VeilarboppfolgingService veilarboppfolgingService;
    @Mock
    private Norg2Client norg2Client;
    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isEnabled(FeatureToggle.VTAO_TILTAK_TOGGLE)).thenReturn(true);
    }

    @Test
    public void kanOppretteVtaoAvtaleTest() {
        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.hentSkrivetilgang(eq(veileder), any(Fnr.class))).thenReturn(new Tilgang.Tillat());

        // TODO: opprett avtale via endepunkt avtaleController.opprettAvtaleSomVeileder()
    }

    @Test
    public void måBesluttesForåGodkjennes() {
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(null);
        var navIdent = avtale.getVeilederNavIdent();
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = new GodkjentPaVegneGrunn();
        godkjentPaVegneGrunn.setDigitalKompetanse(true);
        avtale.godkjennForVeilederOgDeltaker(navIdent, godkjentPaVegneGrunn);
        assertEquals(avtale.getStatus(), Status.MANGLER_GODKJENNING);
    }

    private void værInnloggetSom(Avtalepart<?> avtalepart) {
        lenient().when(innloggingService.hentAvtalepart(any())).thenReturn(avtalepart);
        if (avtalepart instanceof Veileder veileder) {
            lenient().when(innloggingService.hentVeileder()).thenReturn(veileder);
        }
        if (avtalepart instanceof Arbeidsgiver arbeidsgiver) {
            lenient().when(innloggingService.hentArbeidsgiver()).thenReturn(arbeidsgiver);
        }
        if (avtalepart instanceof Beslutter beslutter) {
            lenient().when(innloggingService.hentBeslutter()).thenReturn(beslutter);
        }
    }
}
