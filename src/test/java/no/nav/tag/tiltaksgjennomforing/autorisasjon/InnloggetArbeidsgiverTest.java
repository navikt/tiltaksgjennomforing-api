package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InnloggetArbeidsgiverTest {

    @Mock
    public AvtaleRepository avtaleRepository;

    Avtale avtale = TestData.enArbeidstreningAvtale();
    Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

    @BeforeEach
    public void setUp(){
        avtale.setAnnullertGrunn("Hemmelig");
    }

    @Test
    public void hentAvtale_uten_annullertGrunn() {
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = arbeidsgiver.hentAvtale(avtaleRepository, mock(AvtaleInnholdRepository.class), avtale.getId());
        assertThat(hentetAvtale.getAnnullertGrunn()).isNull();
    }

    @Test
    public void hentAlleAvtalerMedLesetilgang_uten_annullertGrunn() {
        Set<BedriftNr> bedriftNrSet = Set.of(avtale.getBedriftNr());
        when(avtaleRepository.findAllByBedriftNrIn(eq(bedriftNrSet))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = arbeidsgiver.hentAlleAvtalerMedLesetilgang(avtaleRepository, mock(AvtaleInnholdRepository.class), new AvtalePredicate(), Avtale.Fields.sistEndret, 0, Integer.MAX_VALUE);
        assertThat(hentetAvtaler.get(0).getAnnullertGrunn()).isNull();
    }

    @Test
    public void hentAvtalerForMinsideArbeidsgiver_uten_annullertGrunn() {
        when(avtaleRepository.findAllByBedriftNr(eq(avtale.getBedriftNr()))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, avtale.getBedriftNr());
        assertThat(hentetAvtaler.get(0).getAnnullertGrunn()).isNull();
    }
}
