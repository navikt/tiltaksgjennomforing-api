package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.getForPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class VtaoTest {

    @Autowired
    private AvtaleRepository avtaleRepository;
    @Autowired
    private AvtaleController avtaleController;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InnloggingService innloggingService;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private PersondataService persondataService;
    @MockBean
    AxsysService axsysService;
    @Mock
    VeilarbArenaClient veilarbArenaClient;
    @Mock
    Norg2Client norg2Client;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void kanOppretteVtaoAvtaleTest() throws Exception {
        // Lagre en ny VTAO-avtale
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        var lagretAvtale = avtaleRepository.save(avtale);

        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                false,
                veilarbArenaClient
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        // Hent avtalen fra databasen
        //var avtaleResultat = hentAvtaleForVeileder(veileder, lagretAvtale.getId());
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);
        assertEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao().getId(),
                hentetAvtale.getGjeldendeInnhold().getVtao().getId());
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
