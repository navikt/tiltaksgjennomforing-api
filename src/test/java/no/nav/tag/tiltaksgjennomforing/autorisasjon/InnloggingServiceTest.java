package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.ENHET_OPPFØLGING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InnloggingServiceTest {

  @InjectMocks
  private InnloggingService innloggingService;

  @Mock
  private TokenUtils tokenUtils;

  @Mock
  private AltinnTilgangsstyringService altinnTilgangsstyringService;

  @Mock
  private AxsysService axsysService;

  @Mock
  private SystembrukerProperties systembrukerProperties;

  @Mock
  private BeslutterAdGruppeProperties beslutterAdGruppeProperties;

  @Test
  public void hentInnloggetBruker__er_selvbetjeningbruker() {
    InnloggetDeltaker selvbetjeningBruker = TestData.enInnloggetDeltaker();
        værInnloggetDeltaker(selvbetjeningBruker);
        assertThat(innloggingService.hentInnloggetBruker(Avtalerolle.DELTAKER)).isEqualTo(selvbetjeningBruker);
    }

    @Test
    public void hentInnloggetBruker__selvbetjeningbruker_type_arbeidsgiver_skal_hente_organisasjoner() {
        InnloggetArbeidsgiver selvbetjeningBruker = new InnloggetArbeidsgiver(new Fnr("11111111111"), Set.of(), Map.of());
        when(altinnTilgangsstyringService.hentTilganger(selvbetjeningBruker.getIdentifikator())).thenReturn(Map.of());
        when(altinnTilgangsstyringService.hentAltinnOrganisasjoner(selvbetjeningBruker.getIdentifikator())).thenReturn(Set.of());
        værInnloggetArbeidsgiver(selvbetjeningBruker);

        assertThat(innloggingService.hentInnloggetBruker(Optional.of(Avtalerolle.ARBEIDSGIVER).get())).isEqualTo(selvbetjeningBruker);
        verify(altinnTilgangsstyringService).hentTilganger(selvbetjeningBruker.getIdentifikator());
        verify(altinnTilgangsstyringService).hentAltinnOrganisasjoner(selvbetjeningBruker.getIdentifikator());
    }

  @Test
  public void hentInnloggetBruker__er_nav_ansatt_og_har_enhet() {
    InnloggetVeileder navAnsatt = TestData.enInnloggetVeileder();
    when(axsysService.hentEnheterNavAnsattHarTilgangTil(any())).thenReturn(List.of(new NavEnhet(ENHET_OPPFØLGING)));
    værInnloggetVeileder(navAnsatt);

    assertThat(innloggingService.hentInnloggetBruker(Avtalerolle.VEILEDER)).isEqualTo(navAnsatt);
  }

    @Test
    public void hentInnloggetBruker__er_nav_ansatt_og_beslutter() {
        InnloggetBeslutter navAnsatt = TestData.enInnloggetBeslutter();
        værInnloggetBeslutter(navAnsatt);
        assertThat(innloggingService.hentInnloggetBruker(Avtalerolle.BESLUTTER)).isEqualTo(navAnsatt);
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
        when(tokenUtils.hentBrukerOgIssuer())
            .thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_SELVBETJENING, bruker.getIdentifikator().asString())));
    }

    private void værInnloggetArbeidsgiver(InnloggetArbeidsgiver bruker) {
        when(tokenUtils.hentBrukerOgIssuer())
            .thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_SELVBETJENING, bruker.getIdentifikator().asString())));
    }

    private void værInnloggetVeileder(InnloggetVeileder navAnsatt) {
        when(tokenUtils.hentBrukerOgIssuer())
            .thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_ISSO, navAnsatt.getIdentifikator().asString())));
    }

    private void værInnloggetBeslutter(InnloggetBeslutter navAnsatt) {
        when(tokenUtils.harAdGruppe(any())).thenReturn(true);
        when(tokenUtils.hentBrukerOgIssuer())
            .thenReturn(Optional.of(new TokenUtils.BrukerOgIssuer(Issuer.ISSUER_ISSO, navAnsatt.getIdentifikator().asString())));
    }


}
