package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleArenaMigreringTest {

    @Test
    public void lonnstilskudd_tilskuddsperioder_skal_ha_status_ubehandlet_hvis_ikke_ryddeavtale() {
        Now.fixedDate(LocalDate.of(2023, 2, 15));
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate.of(2022, 5, 1), LocalDate.of(2023, 4, 30));
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();

        avtale.getTilskuddPeriode().forEach(tilskuddPeriode ->
                assertThat(tilskuddPeriode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET));
        Now.resetClock();
    }

    @Test
    public void lonnstilskudd_skal_generere_tilskuddsperioder_med_behandlet_status_om_ryddeavtale() {
        Now.fixedDate(LocalDate.of(2023, 2, 15));
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsRyddeAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate.of(2022, 5, 1), LocalDate.of(2023, 4, 30));
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();

        TilskuddPeriode sisteBehandletIArena = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(2011, 12, 31))).findFirst().get();
        assertThat(sisteBehandletIArena.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        TilskuddPeriode førsteUbehandlet = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(2023, 1, 1))).findFirst().get();
        assertThat(førsteUbehandlet.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        Now.resetClock();
    }

    @Test
    public void vtao_skal_generere_tilskuddsperioder_med_behandlet_status_om_ryddeavtale() {
        Now.fixedDate(LocalDate.of(2025, 7, 15));
        Avtale avtale = TestData.enVtaoRyddeAvtaleMedStartOgSluttGodkjentAvAlleParter(
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 12, 5)
        );
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();

        TilskuddPeriode sisteBehandletIArena = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(2011, 12, 31))).findFirst().get();
        assertThat(sisteBehandletIArena.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        TilskuddPeriode førsteUbehandlet = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(2025, 6, 1))).findFirst().get();
        assertThat(førsteUbehandlet.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        Now.resetClock();
    }

    @Test
    public void vtao_skal_generere_tilskuddsperioder_med_behandlet_status_hvis_opphav_arena() {
        Now.fixedDate(LocalDate.of(2025, 5, 15));
        Avtale avtale = TestData.enVtaoArenaAvtaleMedAltUtfylt();

        TilskuddPeriode sisteBehandletIArena = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(
                2011, 12, 31))).findFirst().get();
        assertThat(sisteBehandletIArena.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        TilskuddPeriode førsteUbehandlet = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(
                2025, 6, 1))).findFirst().get();
        assertThat(førsteUbehandlet.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        Now.resetClock();
    }

    @Test
    public void vtao_skal_generere_tilskuddsperioder_med_behandlet_status_ved_arena_migrering() {
        Now.fixedDate(LocalDate.of(2025, 1, 15));
        Avtale avtale = TestData.enVtaoArenaAvtaleMedAltUtfylt();
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(LocalDate.of(2025, 3, 1));

        TilskuddPeriode sisteBehandletIArena = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(
                2011, 12, 31))).findFirst().get();
        assertThat(sisteBehandletIArena.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        TilskuddPeriode førsteUbehandlet = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(
                2025, 3, 1))).findFirst().get();
        assertThat(førsteUbehandlet.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        Now.resetClock();
    }

    @Test
    public void VTAO_skal_ha_status_ubehandlet_hvis_ikke_ryddeavtale() {
        Now.fixedDate(LocalDate.of(2025, 5, 15));
        Avtale avtale = TestData.enVtaoAvtaleMedAltUtfylt();
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();

        avtale.getTilskuddPeriode().forEach(tilskuddPeriode ->
            assertThat(tilskuddPeriode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET));
        Now.resetClock();
    }

    @Test
    public void VTAO_skal_generere_tilskuddsperioder_med_behandlet_status_om_ryddeavtale() {
        Avtale avtale = TestData.enVtaoRyddeAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate.of(2025, 4, 1), LocalDate.of(2026, 4, 1));
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();

        TilskuddPeriode førstetilskudsperiode = avtale.getTilskuddPeriode().first();
        assertThat(førstetilskudsperiode.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        TilskuddPeriode førsteUbehandlet = avtale.getTilskuddPeriode().stream()
            .filter(tilskuddPeriode -> tilskuddPeriode.getStartDato().isAfter(LocalDate.of(2025, 6, 1)))
            .findFirst().get();
        assertThat(førsteUbehandlet.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
    }
}
