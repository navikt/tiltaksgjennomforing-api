package no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
class VeilarbvedtaksstotteServiceTest {

    @Autowired
    private VeilarbvedtaksstotteService veilarbvedtaksstotteService;

    @BeforeEach
    void setUp() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void hent_gjeldende_14a_vedtak() {
        Optional<Gjeldende14aVedtakRespons> vedtak = veilarbvedtaksstotteService.hentGjeldende14aVedtak(Fnr.generer(1990, 4, 12));

        assertThat(vedtak).isPresent();
        assertThat(vedtak.get().innsatsgruppe()).isEqualTo(Innsatsgruppe.TRENGER_VEILEDNING);
        assertThat(vedtak.get().hovedmal()).isEqualTo(Gjeldende14aVedtakRespons.Hovedmal.SKAFFE_ARBEID);
    }

    @Test
    public void returnerer_tomt_resultat_naar_person_ikke_har_gjeldende_vedtak() {
        Optional<Gjeldende14aVedtakRespons> vedtak = veilarbvedtaksstotteService.hentGjeldende14aVedtak(Fnr.generer(1985, 3, 22));

        assertThat(vedtak).isEmpty();
    }
}
