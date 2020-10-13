package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtalePredicate;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InnloggetArbeidsgiverTest {

    @Mock
    public AvtaleRepository avtaleRepository;

    Avtale avtale = TestData.enArbeidstreningAvtale();
    InnloggetArbeidsgiver innloggetArbeidsgiver = TestData.innloggetArbeidsgiver(TestData.enArbeidsgiver(avtale));

    @Before
    public void setUp(){
        avtale.setAvbruttGrunn("Hemmelig");
    }

    @Test
    public void hentAvtale_uten_avbruttGrunn() {
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = innloggetArbeidsgiver.hentAvtale(avtaleRepository, avtale.getId());
        assertThat(hentetAvtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void hentAlleAvtalerMedLesetilgang_uten_avbruGrunn() {
        Set<BedriftNr> bedriftNrSet = new HashSet<>();
        bedriftNrSet.add(avtale.getBedriftNr());
        when(avtaleRepository.findAllByBedriftNrIn(eq(bedriftNrSet))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = innloggetArbeidsgiver.hentAlleAvtalerMedLesetilgang(avtaleRepository, new AvtalePredicate());
        assertThat(hentetAvtaler.get(0).getAvbruttGrunn()).isNull();
    }

    @Test
    public void hentAvtalerForMinsideArbeidsgiver_uten_avbrunn() {
        when(avtaleRepository.findAllByBedriftNr(eq(avtale.getBedriftNr()))).thenReturn(Arrays.asList(avtale));
        List<Avtale> hentetAvtaler = innloggetArbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, avtale.getBedriftNr());
        assertThat(hentetAvtaler.get(0).getAvbruttGrunn()).isNull();
    }
}
