package no.nav.tag.tiltaksgjennomforing.integrasjon;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_tilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.SystembrukerProperties;

@RunWith(MockitoJUnitRunner.class)
public class InnloggingServiceTest {

    @InjectMocks
    private InnloggingService innloggingService;

    @Mock
    private TokenUtils tokenUtils;

    @Mock
    private AltinnTilgangsstyringService altinnTilgangsstyringService;

    @Mock
    private SystembrukerProperties systembrukerProperties;

    @Test
    public void hentInnloggetBruker__er_selvbetjeningbruker() {
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.enSelvbetjeningBruker();
        vaerInnloggetSelvbetjening(selvbetjeningBruker);
        assertThat(innloggingService.hentInnloggetBruker()).isEqualTo(selvbetjeningBruker);
    }
    
    @Test
    public void hentInnloggetBruker__selvbetjeningbruker_skal_hente_organisasjoner() {
        List<Organisasjon> organisasjoner = asList(new Organisasjon(new BedriftNr("111111111"), "Navn"));
        InnloggetSelvbetjeningBruker selvbetjeningBruker = new InnloggetSelvbetjeningBruker(new Fnr("11111111111"), organisasjoner);
        when(altinnTilgangsstyringService.hentOrganisasjoner(selvbetjeningBruker.getIdentifikator())).thenReturn(organisasjoner);
        vaerInnloggetSelvbetjening(selvbetjeningBruker);
        
        assertThat(innloggingService.hentInnloggetBruker()).isEqualTo(selvbetjeningBruker);
        verify(altinnTilgangsstyringService).hentOrganisasjoner(selvbetjeningBruker.getIdentifikator());
    }
    
    @Test
    public void hentInnloggetBruker__er_nav_ansatt() {
        InnloggetNavAnsatt navAnsatt = TestData.enNavAnsatt();
        vaerInnloggetNavAnsatt(navAnsatt);
        assertThat(innloggingService.hentInnloggetBruker()).isEqualTo(navAnsatt);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetNavAnsatt__er_selvbetjeningbruker() {
        vaerInnloggetSelvbetjening(TestData.enSelvbetjeningBruker());
        innloggingService.hentInnloggetNavAnsatt();
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetBruker__er_uinnlogget() {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.empty());
        innloggingService.hentInnloggetNavAnsatt();
    }

    @Test(expected = TilgangskontrollException.class)
    public void avviser_selvbetjeningBruker_som_systemBruker(){
        vaerInnloggetSelvbetjening(TestData.enSelvbetjeningBruker());
        innloggingService.validerSystembruker();
    }

    @Test(expected = TilgangskontrollException.class)
    public void avviser_navAnsatt_som_systemBruker(){
        vaerInnloggetNavAnsatt(TestData.enNavAnsatt());
        innloggingService.validerSystembruker();
    }

    @Test(expected = TilgangskontrollException.class)
    public void avviser_ukjent_systemBruker(){
        vaerInnloggetSystem("ukjent");
        innloggingService.validerSystembruker();
    }    

    @Test
    public void godtar_forventet_systemBruker(){
        vaerInnloggetSystem("forventet");
        innloggingService.validerSystembruker();
    }    

    private void vaerInnloggetSystem(String systemId) {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_SYSTEM, systemId)));
        when(systembrukerProperties.getId()).thenReturn("forventet");
    }

    private void vaerInnloggetSelvbetjening(InnloggetSelvbetjeningBruker bruker) {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_SELVBETJENING, bruker.getIdentifikator().asString())));
    }

    private void vaerInnloggetNavAnsatt(InnloggetNavAnsatt navAnsatt) {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_ISSO, navAnsatt.getIdentifikator().asString())));
    }

    
}
