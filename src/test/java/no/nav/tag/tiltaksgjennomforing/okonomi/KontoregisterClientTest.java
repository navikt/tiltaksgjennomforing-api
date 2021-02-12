package no.nav.tag.tiltaksgjennomforing.okonomi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.exceptions.KontoregisterFeilException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL, "wiremock"})
@DirtiesContext
public class KontoregisterClientTest {
    @Autowired
    private KontoregisterClient KontoregisterClient;

    @Test
    public void hentKontonummer__skal_returnere_verdi_fra_kall() {
        String kontonummerTilbake = KontoregisterClient.hentKontonummer("889640782");
        assertThat(kontonummerTilbake).isEqualTo("10000008162");
    }

    @Test
    public void hentKontonummer__skal_returnere_feilmelding() {
        assertThatThrownBy(() ->  KontoregisterClient.hentKontonummer("111234567"))
            .isInstanceOf(KontoregisterFeilException.class);
    }
}
