package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditConsoleLogger;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.*;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class AvtaleApiTest {

    public AvtaleApiTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    @MockBean
    AxsysService axsysService;
    @Mock
    VeilarboppfolgingService veilarboppfolgingService;
    @Mock
    Norg2Client norg2Client;
    @Autowired
    private AvtaleRepository avtaleRepository;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private PersondataService persondataService;
    @SpyBean
    private AuditConsoleLogger auditConsoleLogger;

    @Test
    public void hentSkalReturnereRiktigAvtale() throws Exception {
        Avtale avtale = enArbeidstreningAvtale();
        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                false,
                veilarboppfolgingService
        );
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(axsysService.hentEnheterNavAnsattHarTilgangTil(any())).thenReturn(List.of());
        avtaleRepository.save(avtale);
        var res = hentAvtaleForVeileder(veileder, avtale.getId());
        assertEquals(200, res.getStatus());
        assertTrue(jsonHarVerdi(res.getContentAsString(), avtale.getDeltakerFnr().asString()));
        assertEquals(avtale.getId().toString(), mapper.readTree(res.getContentAsByteArray()).get("id").asText());

        verify(auditConsoleLogger, times(1)).logg(any());
    }

    @Test
    public void hentBeslutterListe() throws Exception {
        Avtale avtale = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(1), Now.localDate().plusMonths(3).plusDays(1));
        Avtale avtale2 = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(5), Now.localDate().plusMonths(3).plusDays(5));
        Avtale avtale3 = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(10), Now.localDate().plusMonths(3).plusDays(10));
        Avtale avtale4 = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(15), Now.localDate().plusMonths(3).plusDays(15));
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Arne");
        avtale2.getGjeldendeInnhold().setDeltakerFornavn("Bjarne");
        avtale3.getGjeldendeInnhold().setDeltakerFornavn("Carl");

        avtaleRepository.save(avtale);
        avtaleRepository.save(avtale2);
        avtaleRepository.save(avtale3);
        avtaleRepository.save(avtale4);

        var beslutterIdent = TestData.enNavIdent();
        var beslutter = new Beslutter(
                beslutterIdent,
                UUID.randomUUID(),
                Set.of(ENHET_OPPFØLGING),
                tilgangskontrollService,
                norg2Client
        );
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(beslutter), any(Fnr.class))).thenReturn(true);
        when(axsysService.hentEnheterNavAnsattHarTilgangTil(any())).thenReturn(List.of(ENHET_OPPFØLGING));

        var respons = hentAvtaleListeForBeslutterPåNavEnhet(beslutter, ENHET_OPPFØLGING.getVerdi());

        assertFalse(jsonHarVerdi(respons.getContentAsString(), avtale.getDeltakerFnr().asString()));
        assertFalse(jsonHarNøkkel(respons.getContentAsString(), "deltakerFnr"));

        assertEquals(4, ((List<?>) mapper.readValue(respons.getContentAsString(), HashMap.class).get("avtaler")).size());
    }

    private MockHttpServletResponse hentAvtaleForVeileder(Veileder veileder, UUID avtaleId) throws Exception {
        return getForPart(mockMvc, veileder, "/avtaler/" + avtaleId);
    }

    private MockHttpServletResponse hentAvtaleListeForBeslutterPåNavEnhet(Beslutter beslutter, String navEnhet) throws Exception {
        return getForPart(mockMvc, beslutter, "/avtaler/beslutter-liste?navEnhet=%s".formatted(navEnhet));
    }
}
