package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PabegynteAvtalerRyddejobbTest {
    @Mock
    private AvtaleRepository avtaleRepository;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void before() {
        Now.fixedDate(LocalDate.of(2024, 1, 1));
        when(featureToggleService.isEnabled(FeatureToggle.PABEGYNT_AVTALE_RYDDE_JOBB)).thenReturn(true);
    }

    @AfterEach
    void after() {
        Now.resetClock();
    }

    @Test
    void gamle_avtaler_utloper() {
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
            mockAvtale(LocalDateTime.of(2024, 12, 15, 14, 40)),
            mockAvtale(LocalDateTime.of(2025, 5, 19, 11, 20))
        );

        when(avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning()).thenReturn(avtaler);

        PabegynteAvtalerRyddejobb påbegynteAvtalerRyddejobb = new PabegynteAvtalerRyddejobb(featureToggleService, avtaleRepository);
        påbegynteAvtalerRyddejobb.run();

        verify(skalUtlope1, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope2, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope3, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope4, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalUtlope5, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(avtaleRepository, times(5)).save(any());
    }

    @Test
    void utloper_og_varsler() {
        Avtale skalUtlope = mockAvtale(LocalDateTime.of(2023, 10, 8, 12, 0));
        Avtale skalVarsles24Timer = mockAvtale(LocalDateTime.of(2023, 10, 9, 12, 0));
        Avtale skalVarsles1Uke = mockAvtale(LocalDateTime.of(2023, 10, 15, 12, 0));

        List<Avtale> avtaler = List.of(
            skalUtlope,
            skalVarsles24Timer,
            mockAvtale(LocalDateTime.of(2023, 10, 10, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 11, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 12, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 13, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 14, 12, 0)),
            skalVarsles1Uke
        );

        when(avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning()).thenReturn(avtaler);

        PabegynteAvtalerRyddejobb påbegynteAvtalerRyddejobb = new PabegynteAvtalerRyddejobb(featureToggleService, avtaleRepository);
        påbegynteAvtalerRyddejobb.run();

        verify(skalUtlope, times(1)).utlop(AvtaleUtlopHandling.UTLOP);
        verify(skalVarsles24Timer, times(1)).utlop(AvtaleUtlopHandling.VARSEL_24_TIMER);
        verify(skalVarsles1Uke, times(1)).utlop(AvtaleUtlopHandling.VARSEL_EN_UKE);
        verify(avtaleRepository, times(3)).save(any());
    }

    @Test
    void varsler_at_det_er_24_timer_igjen() {
        Avtale skalVarsles24Timer = mockAvtale(LocalDateTime.of(2023, 10, 9, 12, 0));

        List<Avtale> avtaler = List.of(
            skalVarsles24Timer,
            mockAvtale(LocalDateTime.of(2023, 10, 10, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 11, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 12, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 14, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 16, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 17, 12, 0))
        );

        when(avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning()).thenReturn(avtaler);

        PabegynteAvtalerRyddejobb påbegynteAvtalerRyddejobb = new PabegynteAvtalerRyddejobb(featureToggleService, avtaleRepository);
        påbegynteAvtalerRyddejobb.run();

        verify(skalVarsles24Timer, times(1)).utlop(AvtaleUtlopHandling.VARSEL_24_TIMER);
        verify(avtaleRepository, times(1)).save(any());
    }

    @Test
    void varsler_at_det_er_1_uke_igjen() {
        Avtale skalVarsles1Uke = mockAvtale(LocalDateTime.of(2023, 10, 15, 12, 0));

        List<Avtale> avtaler = List.of(
            mockAvtale(LocalDateTime.of(2023, 10, 10, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 11, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 12, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 13, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 14, 12, 0)),
            skalVarsles1Uke,
            mockAvtale(LocalDateTime.of(2023, 10, 16, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 17, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 18, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 19, 12, 0)),
            mockAvtale(LocalDateTime.of(2023, 10, 20, 12, 0))
        );

        when(avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning()).thenReturn(avtaler);

        PabegynteAvtalerRyddejobb påbegynteAvtalerRyddejobb = new PabegynteAvtalerRyddejobb(featureToggleService, avtaleRepository);
        påbegynteAvtalerRyddejobb.run();

        verify(skalVarsles1Uke, times(1)).utlop(AvtaleUtlopHandling.VARSEL_EN_UKE);
        verify(avtaleRepository, times(1)).save(any());
    }

    private static Avtale mockAvtale(LocalDateTime sistEndret) {
        Avtale avtale = Mockito.mock(Avtale.class);
        when(avtale.getSistEndret()).thenReturn(ZonedDateTime.of(sistEndret, ZoneId.systemDefault()).toInstant());
        return avtale;
    }

}
