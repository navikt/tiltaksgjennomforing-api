package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.metrikker.MetrikkRegistrering;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(Miljø.LOCAL)
@DirtiesContext
public class AvtaleRepositoryTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @MockBean
    private MetrikkRegistrering metrikkRegistrering;

    @Test
    public void nyAvtaleSkalKunneLagreOgReturneresAvRepository() {
        Avtale lagretAvtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        Optional<Avtale> avtaleOptional = avtaleRepository.findById(lagretAvtale.getId());
        assertThat(avtaleOptional).isPresent();
    }

    @Test
    public void skalKunneLagreMaalFlereGanger() {
        // Lage avtale
        Avtale lagretAvtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        Maal maal = TestData.etMaal();
        endreAvtale.setMaal(List.of(maal));
        lagretAvtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale);

        // Lage ny avtale
        Avtale lagretAvtale2 = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal enda fungere
        EndreAvtale endreAvtale2 = new EndreAvtale();
        Maal maal2 = TestData.etMaal();
        endreAvtale2.setMaal(List.of(maal2));
        lagretAvtale2.endreAvtale(Instant.now(), endreAvtale2, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale2);
    }

    @Test
    public void skalKunneLagreOppgaverFlereGanger() {
        // Lage avtale
        Avtale lagretAvtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        lagretAvtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale);

        // Lage ny avtale
        Avtale lagretAvtale2 = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal enda fungere
        EndreAvtale endreAvtale2 = new EndreAvtale();
        lagretAvtale2.endreAvtale(Instant.now(), endreAvtale2, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale2);
    }

    @Test
    public void skalKunneLagreTilskuddsPeriode() {
        // Lage avtale
        Avtale lagretAvtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        lagretAvtale.setSumLonnstilskudd(20000);
        avtaleRepository.save(lagretAvtale);

        // Lagre tilskuddsperiode skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setStartDato(lagretAvtale.getStartDato());
        endreAvtale.setSluttDato(lagretAvtale.getSluttDato());
        endreAvtale.setManedslonn(20000);
        endreAvtale.setStillingprosent(100);
        endreAvtale.setFeriepengesats(BigDecimal.valueOf(0.12));
        endreAvtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        endreAvtale.setLonnstilskuddProsent(40);

        lagretAvtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        Avtale nyLagretAvtale = avtaleRepository.save(lagretAvtale);

        List<TilskuddPeriode> perioder = nyLagretAvtale.getTilskuddPeriode();
        assertThat(perioder).isNotEmpty();
        assertThat(lagretAvtale.getVersjoner().get(0).getId()).isEqualTo(perioder.get(0).getAvtaleInnhold().getId());
    }

    @Test
    public void avtale_godkjent_pa_vegne_av_skal_lagres_med_pa_vegne_av_grunn() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        godkjentPaVegneGrunn.setIkkeBankId(true);
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);

        assertThat(lagretAvtale.getGodkjentPaVegneGrunn().isIkkeBankId()).isEqualTo(godkjentPaVegneGrunn.isIkkeBankId());
    }

    @Test
    public void lagre_pa_vegne_skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);

        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentPaVegneAv(any());
    }

    @Test
    public void opprettAvtale__skal_publisere_domainevent() {
        Avtale nyAvtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("10101033333"), new BedriftNr("101033333"), Tiltakstype.ARBEIDSTRENING), new NavIdent("Q000111"));
        avtaleRepository.save(nyAvtale);
        verify(metrikkRegistrering).avtaleOpprettet(any());
    }

    @Test
    public void endreAvtale__skal_publisere_domainevent() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering, never()).avtaleEndret(any());
        avtale.endreAvtale(Instant.now(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).avtaleEndret(any());
    }

    @Test
    public void godkjennForArbeidsgiver__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TestData.enArbeidsgiver(avtale).godkjennAvtale(avtale.getSistEndret(), avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentAvArbeidsgiver(any());
    }

    @Test
    public void godkjennForDeltaker__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TestData.enDeltaker(avtale).godkjennAvtale(avtale.getSistEndret(), avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentAvDeltaker(any());
    }

    @Test
    public void godkjennForVeileder__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        TestData.enVeileder(avtale).godkjennAvtale(avtale.getSistEndret(), avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentAvVeileder(any());
    }

    @Test
    public void opphevGodkjenning__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TestData.enVeileder(avtale).opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjenningerOpphevet(any(GodkjenningerOpphevetAvVeileder.class));
    }
}
