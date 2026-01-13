package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.AvtaleDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        AvtaleDTO hentetAvtale = new AvtaleDTO(arbeidsgiver.hentAvtale(avtaleRepository, avtale.getId())).maskerFelterForAvtalePart(arbeidsgiver);
        assertThat(hentetAvtale.annullertGrunn()).isNull();
    }

    @Test
    public void hentAvtalerForMinsideArbeidsgiver_uten_annullertGrunn() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setAnnullertGrunn("Hemmelig");

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        when(avtaleRepository.findAllByBedriftNr(eq(Set.of(avtale.getBedriftNr())), any())).thenReturn(new PageImpl<>(List.of(avtale)));
        List<AvtaleDTO> hentetAvtaler = arbeidsgiver.hentAvtalerForMinSideArbeidsgiver(avtaleRepository, avtale.getBedriftNr());
        assertThat(hentetAvtaler.getFirst().annullertGrunn()).isNull();
    }
}
