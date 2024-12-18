package no.nav.tag.tiltaksgjennomforing.orgenhet;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
@EmbeddedKafka
public class EregServiceTest {
    @Autowired
    private EregService eregService;

    @Test
    public void hentBedriftNavn__returnerer_navn_og_bedriftnr() {
        Organisasjon organisasjon = eregService.hentVirksomhet(new BedriftNr("999999999"));
        assertThat(organisasjon.getBedriftNr()).isEqualTo(new BedriftNr("999999999"));
        assertThat(organisasjon.getBedriftNavn()).isEqualTo("Saltrød og Høneby");
    }

    @Test
    public void hentBedriftNavn__kaster_exception_ved_404() {
        assertThatThrownBy(() -> eregService.hentVirksomhet(new BedriftNr("699999999"))).isExactlyInstanceOf(EnhetFinnesIkkeException.class);
    }

    @Test
    public void hentBedriftNavn__kaster_exception_dersom_enhet_er_slettet() {
        assertThatThrownBy(() -> eregService.hentVirksomhet(new BedriftNr("799999999"))).isExactlyInstanceOf(EnhetErSlettetException.class);
    }
}
