package no.nav.tag.tiltaksgjennomforing.avtale.service;

import jakarta.transaction.Transactional;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PabegynteAvtalerRyddeServiceTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Mock
    private AvtaleRepository avtaleRepositoryMock;

    @Mock
    private FeatureToggleService featureToggleServiceMock;

    @BeforeEach
    void beforeEach() {
        when(featureToggleServiceMock.isEnabled(FeatureToggle.PABEGYNT_AVTALE_RYDDE_JOBB)).thenReturn(true);
    }

    @AfterEach
    void afterEach() {
        Now.resetClock();
    }

    @Test
    void utloper_gamle_avtaler() {
        Now.fixedDate(LocalDate.of(2024, 11, 29));

        Avtale skalUtlope1 = mockAvtale(LocalDateTime.of(1989, 10, 8, 12, 0));
        Avtale skalUtlope2 = mockAvtale(LocalDateTime.of(2000, 12, 31, 23, 59));
        Avtale skalUtlope3 = mockAvtale(LocalDateTime.of(2019, 3, 11, 19, 18));
        Avtale skalUtlope4 = mockAvtale(LocalDateTime.of(2021, 2, 28, 19, 18));
        Avtale skalUtlope5 = mockAvtale(LocalDateTime.of(2023, 10, 8, 12, 0));

        List<Avtale> avtaler = List.of(
            skalUtlope1,
            skalUtlope2,
            skalUtlope3,
            skalUtlope4,
            skalUtlope5,
            mockAvtale(LocalDateTime.of(2024, 12, 24, 14, 40)),
            mockAvtale(LocalDateTime.of(2025, 5, 19, 11, 20))
        );

        when(avtaleRepositoryMock.findAvtalerSomErPabegyntEllerManglerGodkjenning()).thenReturn(avtaler);

        PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService = new PabegynteAvtalerRyddeService(
            avtaleRepositoryMock,
            featureToggleServiceMock
        );
        pabegynteAvtalerRyddeService.ryddAvtalerSomErPabegyntEllerManglerGodkjenning();

        verify(skalUtlope1, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope2, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope3, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope4, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope5, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(avtaleRepositoryMock, times(5)).save(any());
    }

    @Test
    void skal_varsle_og_utlope() {
        Now.fixedDate(LocalDate.of(2025, 1, 1));

        Avtale skalUtlope = mockAvtale(LocalDateTime.of(2024, 10, 8, 12, 0));
        Avtale skalVarsles24Timer = mockAvtale(LocalDateTime.of(2024, 10, 9, 12, 0));
        Avtale skalVarsles1Uke = mockAvtale(LocalDateTime.of(2024, 10, 15, 12, 0));

        List<Avtale> avtaler = List.of(
            skalUtlope,
            skalVarsles24Timer,
            mockAvtale(LocalDateTime.of(2024, 10, 10, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 11, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 12, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 13, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 14, 12, 0)),
            skalVarsles1Uke,
            mockAvtale(LocalDateTime.of(2024, 10, 16, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 17, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 18, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 19, 12, 0)),
            mockAvtale(LocalDateTime.of(2024, 10, 20, 12, 0))
        );

        when(avtaleRepositoryMock.findAvtalerSomErPabegyntEllerManglerGodkjenning()).thenReturn(avtaler);

        PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService = new PabegynteAvtalerRyddeService(
            avtaleRepositoryMock,
            featureToggleServiceMock
        );
        pabegynteAvtalerRyddeService.ryddAvtalerSomErPabegyntEllerManglerGodkjenning();

        verify(skalUtlope, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalVarsles24Timer, times(1)).utlop(AvtaleUtlopHandling.VARSEL_24_TIMER);
        verify(skalVarsles1Uke, times(1)).utlop(AvtaleUtlopHandling.VARSEL_EN_UKE);
        verify(avtaleRepositoryMock, times(3)).save(any());
    }

    @Test
    @Transactional
    void tar_bare_avtaler_som_er_pabegynt_eller_mangler_godkjenning() {
        PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService = new PabegynteAvtalerRyddeService(
            avtaleRepository,
            featureToggleServiceMock
        );

        Now.fixedDate(LocalDate.of(2025, 1, 1));

        Avtale avtale1  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale1.setStatus(Status.ANNULLERT);
        avtale1.setSistEndret(ZonedDateTime.of(2024, 10, 8, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale1);

        Avtale avtale2  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale2.setStatus(Status.AVBRUTT);
        avtale2.setSistEndret(ZonedDateTime.of(2024, 10, 7, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale2);

        Avtale avtale3  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale3.setStatus(Status.KLAR_FOR_OPPSTART);
        avtale3.setSistEndret(ZonedDateTime.of(2024, 10, 6, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale3);

        Avtale avtale4  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale4.setStatus(Status.GJENNOMFØRES);
        avtale4.setSistEndret(ZonedDateTime.of(2024, 10, 5, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale4);

        Avtale avtale5  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale5.setStatus(Status.AVSLUTTET);
        avtale5.setSistEndret(ZonedDateTime.of(2024, 10, 4, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale5);

        Avtale avtale6  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale6.setStatus(Status.MANGLER_GODKJENNING);
        avtale6.setSistEndret(ZonedDateTime.of(2024, 10, 3, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale6);

        Avtale avtale7  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale7.setStatus(Status.PÅBEGYNT);
        avtale7.setSistEndret(ZonedDateTime.of(2024, 10, 2, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant());
        avtaleRepository.save(avtale7);

        pabegynteAvtalerRyddeService.ryddAvtalerSomErPabegyntEllerManglerGodkjenning();

        assertThat(avtaleRepository.findById(avtale1.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.ANNULLERT);
        assertThat(avtaleRepository.findById(avtale2.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.AVBRUTT);
        assertThat(avtaleRepository.findById(avtale3.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.KLAR_FOR_OPPSTART);
        assertThat(avtaleRepository.findById(avtale4.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.GJENNOMFØRES);
        assertThat(avtaleRepository.findById(avtale5.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.AVSLUTTET);
        assertThat(avtaleRepository.findById(avtale6.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.ANNULLERT);
        assertThat(avtaleRepository.findById(avtale7.getId()).map(Avtale::getStatus).orElse(null)).isEqualTo(Status.ANNULLERT);
    }

    private static Avtale mockAvtale(LocalDateTime sistEndret) {
        return mockAvtale(sistEndret, Math.random() > 0.5 ? Status.PÅBEGYNT : Status.MANGLER_GODKJENNING);
    }

    private static Avtale mockAvtale(LocalDateTime sistEndret, Status status) {
        Avtale avtale = Mockito.mock(Avtale.class);
        when(avtale.getSistEndret()).thenReturn(ZonedDateTime.of(sistEndret, ZoneId.systemDefault()).toInstant());
        when(avtale.getStatus()).thenReturn(status);
        return avtale;
    }

}
