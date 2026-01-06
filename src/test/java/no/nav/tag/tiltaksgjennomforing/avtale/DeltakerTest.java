package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles(Miljø.TEST)
public class DeltakerTest {

    @MockBean
    private AvtaleRepository avtaleRepository;

    @Test
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        assertThatThrownBy(() -> deltaker.opphevGodkjenninger(avtale)).isInstanceOf(KanIkkeOppheveException.class);
    }

    @Test
    public void mentor_en_Avtale__skjul_mentor_fnr_for_deltaker() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        when(avtaleRepository.findById(any())).thenReturn(Optional.of(avtale));

        Avtale avtaler = deltaker.hentAvtale(avtaleRepository, avtale.getId());
        assertThat(avtaler.getMentorFnr()).isNull();
        assertThat(avtaler.getGjeldendeInnhold().getMentorTimelonn()).isNull();
    }

    @Test
    public void deltaker_alder_ikke_eldre_enn_72() {
        Now.fixedDate(LocalDate.of(2021, 1, 20));
        Avtale avtale = Avtale.opprett(new OpprettAvtale(new Fnr("30015521534"), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2028, 1, 30));
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER));

        Now.resetClock();
    }

    @Test
    public void deltaker_alder_ikke_eldre_enn_67_for_VTAO_avtale_godkjent_for_veileder_og_deltaker() {
        Now.fixedDate(LocalDate.of(2025, 1, 9));
        Avtale avtale = Avtale.opprett(
            new OpprettAvtale(
                new Fnr("13529543640"),
                TestData.etBedriftNr(),
                Tiltakstype.VTAO
            ), Avtaleopphav.VEILEDER, TestData.enNavIdent()
        );
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2025, 1, 9));
        endreAvtale.setSluttDato(LocalDate.of(2070, 3, 30));
        assertFeilkode(Feilkode.DELTAKER_67_AAR, () -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER));

        Now.resetClock();
    }

    @Test
    public void deltaker_alder_ikke_eldre_enn_67_for_VTAO_avtale_godkjent_av_veileder() {
        Now.fixedDate(LocalDate.now());
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiveruUtenEndringer();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Instant.now());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Instant.now());
        assertFeilkode(Feilkode.DELTAKER_67_AAR, () -> avtale.godkjennForVeileder(TestData.enNavIdent()));

        Now.resetClock();
    }

    @Test
    public void deltaker_skal_ikke_se_avtaler_opprettet_av_arena_som_ikke_er_inngatt() {
        Avtale avtale = Avtale.opprett(
            new OpprettAvtale(
                new Fnr("00000000000"),
                TestData.etBedriftNr(),
                Tiltakstype.MENTOR
            ), Avtaleopphav.ARENA, TestData.enNavIdent()
        );

        Deltaker deltaker = TestData.enDeltaker(avtale);
        when(avtaleRepository.findById(any())).thenReturn(Optional.of(avtale));

        assertThatThrownBy(() -> deltaker.hentAvtale(avtaleRepository, avtale.getId()))
            .isInstanceOf(RessursFinnesIkkeException.class);

        when(avtaleRepository.findAllByDeltakerFnrAndFeilregistrertIsFalse(any(), any())).thenReturn(
            new PageImpl<>(List.of(avtale))
        );

        assertThat(deltaker.hentAvtalerMedLesetilgang(
            avtaleRepository,
            new AvtaleQueryParameter(),
            Pageable.ofSize(10)
        ).getContent().size()).isEqualTo(0);
    }

}
