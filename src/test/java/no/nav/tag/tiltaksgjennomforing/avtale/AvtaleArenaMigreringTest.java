package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleArenaMigreringTest {
    @Test
    public void avtale_med_tilskuddsperioder_skal_ikke_generere_nye_eller_endre() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        int antallPerioderFørKall = avtale.getTilskuddPeriode().size();
        avtale.nyeTilskuddsperioderPåAvtalerFraArena(Now.localDate());
        int antallPerioderEtterKall = avtale.getTilskuddPeriode().size();

        assertThat(antallPerioderFørKall).isEqualTo(antallPerioderEtterKall);
    }

    @Test
    public void avtale_uten_tilskuddsperioder_får_generert_nye_ved_migrering() {
        Now.fixedDate(LocalDate.of(2022, 11, 15));
        Avtale avtale = TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().minusMonths(6), Now.localDate().plusMonths(12));
        avtale.getTilskuddPeriode().clear();
        assertThat(avtale.getTilskuddPeriode()).isEmpty();
        avtale.nyeTilskuddsperioderPåAvtalerFraArena(LocalDate.of(2023, 02, 01));
        System.out.println("Antall " + avtale.getTilskuddPeriode().size());
        avtale.getTilskuddPeriode().forEach(periode -> System.out.println(periode.getStatus() + " " + periode.getStartDato() + " " + periode.getSluttDato() + " " + periode.getLonnstilskuddProsent()));
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();
    }

    @Test
    public void tilskuddsperioder_etter_migrering_med_status_behandlet_i_arena() {
        Now.fixedDate(LocalDate.of(2022, 11, 15));
        Avtale avtale = TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().minusMonths(6), Now.localDate().plusMonths(12));
        avtale.getTilskuddPeriode().clear();
        avtale.nyeTilskuddsperioderPåAvtalerFraArena(LocalDate.of(2023, 02, 01));

        TilskuddPeriode førsteUbehandlet = avtale.getTilskuddPeriode().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET).findFirst().get();
        assertThat(førsteUbehandlet.getStartDato()).isEqualTo(LocalDate.of(2023,02,01));
        assertThat(førsteUbehandlet.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        TilskuddPeriode sisteBehandletIArena = avtale.getTilskuddPeriode()
                .stream()
                .filter(tilskuddPeriode -> tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA)
                .reduce((first, second) -> second).orElse(null);

        assertThat(sisteBehandletIArena.getStartDato()).isEqualTo(LocalDate.of(2023,01,01));
        assertThat(sisteBehandletIArena.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
    }
}
