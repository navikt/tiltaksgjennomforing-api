package no.nav.tag.tiltaksgjennomforing.brev;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.brev.digitalkontaktinformasjon.DigitalKontaktinformasjonClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.brev.postadresse.PostadresseClient;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostutsendelseServiceTest {
    private static final Fnr FNR = Fnr.fraDb("12345678910");

    @Mock
    private PostadresseClient postadresseClient;

    @Mock
    private DigitalKontaktinformasjonClient digitalKontaktinformasjonClient;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private PostutsendelseService postutsendelseService;

    @BeforeEach
    public void setup() {
        when(featureToggleService.isEnabled(FeatureToggle.SJEKK_AT_DELTAKER_KAN_MOTTA_POST)).thenReturn(true);
    }

    @Test
    public void sjekkAtPersonKanMottaPost__skal_ikke_feile_nar_person_har_adresse_og_ikke_er_reservert() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(true);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(false);

        assertThatCode(() -> postutsendelseService.sjekkAtPersonKanMottaPost(FNR)).doesNotThrowAnyException();
    }

    @Test
    public void sjekkAtPersonKanMottaPost__skal_feile_nar_person_mangler_adresse_og_er_reservert() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(false);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(true);

        assertFeilkode(
            Feilkode.KAN_IKKE_SENDE_POST_MANGLER_ADRESSE_OG_RESERVERT,
            () -> postutsendelseService.sjekkAtPersonKanMottaPost(FNR)
        );
    }

    @Test
    public void sjekkAtPersonKanMottaPost__skal_ikke_feile_nar_person_kun_mangler_adresse() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(false);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(false);

        assertThatCode(() -> postutsendelseService.sjekkAtPersonKanMottaPost(FNR)).doesNotThrowAnyException();
    }

    @Test
    public void sjekkAtPersonKanMottaPost__skal_ikke_feile_nar_person_kun_er_reservert() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(true);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(true);

        assertThatCode(() -> postutsendelseService.sjekkAtPersonKanMottaPost(FNR)).doesNotThrowAnyException();
    }

    @Test
    public void sjekkAtPersonKanMottaPost__skal_ikke_sjekke_postutsendelse_nar_toggle_er_av() {
        when(featureToggleService.isEnabled(FeatureToggle.SJEKK_AT_DELTAKER_KAN_MOTTA_POST)).thenReturn(false);

        assertThatCode(() -> postutsendelseService.sjekkAtPersonKanMottaPost(FNR)).doesNotThrowAnyException();

        verify(postadresseClient, never()).sjekkOmPersonErRegistrertMedAdresse(FNR);
        verify(digitalKontaktinformasjonClient, never()).erPersonReservertMotDigitalKontakt(FNR);
    }
}






