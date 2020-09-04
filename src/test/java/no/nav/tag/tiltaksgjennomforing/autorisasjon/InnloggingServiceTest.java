package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;

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
        InnloggetDeltaker selvbetjeningBruker = TestData.enInnloggetDeltaker();
        værInnloggetDeltaker(selvbetjeningBruker);
        assertThat(innloggingService.hentInnloggetBruker(Avtalerolle.DELTAKER)).isEqualTo(selvbetjeningBruker);
    }
    
    @Test
    public void hentInnloggetBruker__selvbetjeningbruker_type_arbeidsgiver_skal_hente_organisasjoner() {
        List<ArbeidsgiverOrganisasjon> organisasjoner = asList(new ArbeidsgiverOrganisasjon(new BedriftNr("111111111"), "Navn"));
        InnloggetArbeidsgiver selvbetjeningBruker = new InnloggetArbeidsgiver(new Fnr("11111111111"), organisasjoner);
        when(altinnTilgangsstyringService.hentOrganisasjoner(selvbetjeningBruker.getIdentifikator())).thenReturn(organisasjoner);
        værInnloggetArbeidsgiver(selvbetjeningBruker);

        assertThat(innloggingService.hentInnloggetBruker(Optional.of(Avtalerolle.ARBEIDSGIVER).get())).isEqualTo(selvbetjeningBruker);
        verify(altinnTilgangsstyringService).hentOrganisasjoner(selvbetjeningBruker.getIdentifikator());
    }
    
    @Test
    public void hentInnloggetBruker__er_nav_ansatt() {
        InnloggetVeileder navAnsatt = TestData.enInnloggetVeileder();
        værInnloggetVeileder(navAnsatt);
        assertThat(innloggingService.hentInnloggetBruker(Avtalerolle.VEILEDER)).isEqualTo(navAnsatt);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetNavAnsatt__er_selvbetjeningbruker() {
        værInnloggetArbeidsgiver(TestData.enInnloggetArbeidsgiver());
        innloggingService.hentInnloggetVeileder();
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetBruker__er_uinnlogget() {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.empty());
        innloggingService.hentInnloggetVeileder();
    }

    @Test(expected = TilgangskontrollException.class)
    public void avviser_selvbetjeningBruker_som_systemBruker(){
        værInnloggetArbeidsgiver(TestData.enInnloggetArbeidsgiver());
        innloggingService.validerSystembruker();
    }

    @Test(expected = TilgangskontrollException.class)
    public void avviser_navAnsatt_som_systemBruker(){
        værInnloggetVeileder(TestData.enInnloggetVeileder());
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

    private void værInnloggetDeltaker(InnloggetDeltaker bruker) {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_SELVBETJENING, bruker.getIdentifikator().asString())));
    }

    private void værInnloggetArbeidsgiver(InnloggetArbeidsgiver bruker) {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_SELVBETJENING, bruker.getIdentifikator().asString())));
    }

    private void værInnloggetVeileder(InnloggetVeileder navAnsatt) {
        when(tokenUtils.hentBrukerOgIssuer()).thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_ISSO, navAnsatt.getIdentifikator().asString())));
    }

    
}
