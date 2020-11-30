package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InnloggetArbeidsgiverTest {

    @Mock
    public AvtaleRepository avtaleRepository;

    Avtale avtale = TestData.enArbeidstreningAvtale();
    Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

    @Before
    public void setUp(){
        avtale.setAvbruttGrunn("Hemmelig");
    }

    @Test
    public void hentAvtale_uten_avbruttGrunn() {
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = arbeidsgiver.hentAvtale(avtaleRepository, avtale.getId());
        assertThat(hentetAvtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void hentAlleAvtalerMedLesetilgang_uten_avbruttGrunn() {
        Set<BedriftNr> bedriftNrSet = Set.of(avtale.getBedriftNr());
        when(avtaleRepository.findAllByBedriftNrIn(eq(bedriftNrSet))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = arbeidsgiver.hentAlleAvtalerMedLesetilgang(avtaleRepository, new AvtalePredicate());
        assertThat(hentetAvtaler.get(0).getAvbruttGrunn()).isNull();
    }

    @Test
    public void hentAvtalerForMinsideArbeidsgiver_uten_avbruttGrunn() {
        when(avtaleRepository.findAllByBedriftNr(eq(avtale.getBedriftNr()))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, avtale.getBedriftNr());
        assertThat(hentetAvtaler.get(0).getAvbruttGrunn()).isNull();
    }
}
