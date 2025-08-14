package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InnloggetArbeidsgiverTest {

    @Mock
    public AvtaleRepository avtaleRepository;

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void hentAvtale_uten_annullertGrunn() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setAnnullertGrunn("Hemmelig");

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = arbeidsgiver.hentAvtale(avtaleRepository, avtale.getId());
        assertThat(hentetAvtale.getAnnullertGrunn()).isNull();
    }

    @Test
    public void hentAvtalerForMinsideArbeidsgiver_uten_annullertGrunn() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setAnnullertGrunn("Hemmelig");

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        when(avtaleRepository.findAllByBedriftNrAndFeilregistrertIsFalse(eq(avtale.getBedriftNr()))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, avtale.getBedriftNr());
        assertThat(hentetAvtaler.get(0).getAnnullertGrunn()).isNull();
    }
}
