package no.nav.tag.tiltaksgjennomforing.brev;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.digitalkontaktinformasjon.DigitalKontaktinformasjonClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.postadresse.PostadresseClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostutsendelseServiceTest {
    private static final Fnr FNR = Fnr.fraDb("12345678910");

    @Mock
    private PostadresseClient postadresseClient;

    @Mock
    private DigitalKontaktinformasjonClient digitalKontaktinformasjonClient;

    @InjectMocks
    private PostutsendelseService postutsendelseService;

    @Test
    public void sjekkOmBrukerKanFaaPost__skal_returnere_true_nar_person_har_adresse_og_ikke_er_reservert() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(true);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(false);

        assertThat(postutsendelseService.sjekkOmBrukerKanFaaPost(FNR)).isTrue();
    }

    @Test
    public void sjekkOmBrukerKanFaaPost__skal_feile_nar_person_mangler_adresse_og_er_reservert() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(false);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(true);

        assertFeilkode(
            Feilkode.KAN_IKKE_SENDE_POST_MANGLER_ADRESSE_OG_RESERVERT,
            () -> postutsendelseService.sjekkOmBrukerKanFaaPost(FNR)
        );
    }

    @Test
    public void sjekkOmBrukerKanFaaPost__skal_ikke_feile_nar_person_kun_mangler_adresse() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(false);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(false);

        assertThat(postutsendelseService.sjekkOmBrukerKanFaaPost(FNR)).isTrue();
    }

    @Test
    public void sjekkOmBrukerKanFaaPost__skal_ikke_feile_nar_person_kun_er_reservert() {
        when(postadresseClient.sjekkOmPersonErRegistrertMedAdresse(FNR)).thenReturn(true);
        when(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(FNR)).thenReturn(true);

        assertThat(postutsendelseService.sjekkOmBrukerKanFaaPost(FNR)).isTrue();
    }
}






