package no.nav.tag.tiltaksgjennomforing.oppfolging;

import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OppfolgingTest {
    @AfterEach
    void teardown() {
        Now.resetClock();
    }

    @Test
    void oppfolgingGjelderFraFoersteIMndSelvOmAvtaleStarterMidtIMnd() {
        // Vi lager en avtale som starter midt i januar. Ikke etterregistrert.
        // Da forventer vi at første varselstidspunkt er 1. juni, og frist er 31. juli.

        Now.fixedDate(LocalDate.of(2024, 1, 1));

        var startDato = LocalDate.of(2024, 1, 15);
        var sluttDato = LocalDate.of(2025, 1, 1);

        var oppfolging = new Oppfolging(null, startDato, sluttDato)
            .neste();

        assertEquals(oppfolging.getVarselstidspunkt(), LocalDate.of(2024, 6, 1));
        assertEquals(oppfolging.getOppfolgingsfrist(), LocalDate.of(2024, 7, 31));
    }

    @Test
    void etterregistrertAvtaleHarSenereOppfolging() {
        // Vi lager en avtale som starter 1. juni. Den er etterregistrert med 6mnd.
        // Da forventer vi at neste oppfølging er et helt år etter avtalens startdato.

        Now.fixedDate(LocalDate.of(2025, 1, 1));

        var startDato = LocalDate.of(2024, 6, 1);
        var sluttDato = LocalDate.of(2025, 12, 31);

        var oppfolging = new Oppfolging(null, startDato, sluttDato).neste();
        var foersteVarselstidspunkt = oppfolging.getVarselstidspunkt();

        assertEquals(foersteVarselstidspunkt, LocalDate.of(2025, 6, 1));
        assertEquals(oppfolging.getOppfolgingsfrist(), LocalDate.of(2025, 7, 31));
    }

    @Test
    void oppfolgingGjelderFraFoersteIMnd() {
        // Vi lager en avtale som starter 1. januar. Ikke etterregistrert.
        // Da forventer vi at første varselstidspunkt er 1. juni, og frist er 31. juli.
        Now.fixedDate(LocalDate.of(2024, 1, 1));

        var startDato = LocalDate.of(2024, 1, 1);
        var sluttDato = LocalDate.of(2025, 1, 1);

        var oppfolging = new Oppfolging(null, startDato, sluttDato).neste();
        var foersteVarselstidspunkt = oppfolging.getVarselstidspunkt();

        assertEquals(foersteVarselstidspunkt, LocalDate.of(2024, 6, 1));
        assertEquals(oppfolging.getOppfolgingsfrist(), LocalDate.of(2024, 7, 31));
        Now.resetClock();
    }

    @Test
    void oppfolgingLengeOverFristFlytterVarselstidspunkt() {
        // Dersom vi følger opp en avtale 1 mnd etter forrige frist skal ny frist også være forskjøvet med 1 mnd
        Now.fixedDate(LocalDate.of(2024, 1, 1));

        var startDato = LocalDate.of(2024, 1, 1);
        var sluttDato = LocalDate.of(2026, 1, 1);

        var oppfolging = new Oppfolging(null, startDato, sluttDato).neste();
        var fristISluttenAvJuli = oppfolging.getOppfolgingsfrist();
        assertEquals(fristISluttenAvJuli, LocalDate.of(2024, 7, 31));

        Now.fixedDate(LocalDate.of(2024, 9, 1));
        var oppfolgingOverFrist = oppfolging.neste();
        assertEquals(
            oppfolgingOverFrist.getVarselstidspunkt(),
            LocalDate.of(2025, 2, 1)
        );
        assertEquals(
            oppfolgingOverFrist.getOppfolgingsfrist(),
            LocalDate.of(2025, 3, 31)
        );
    }

    @Test
    void oppfolgingStarterAldriPaaKortAvtale() {
        // Dersom en avtale varer kortere enn 6mnd så vil oppfølging aldri forekomme.
        Now.fixedDate(LocalDate.of(2024, 1, 1));

        var startDato = LocalDate.of(2024, 1, 1);
        var sluttDato = LocalDate.of(2024, 6, 1);

        var avtaleStarterIJanuar = new Oppfolging(null, startDato, sluttDato).neste();

        assertNull(avtaleStarterIJanuar.getVarselstidspunkt());
    }

    @Test
    void oppfolgingStopperPaaAvtale() {
        // Dersom vi følger opp en avtale før frist, og neste frist vil være etter avtalens slutt,
        // skal vi ikke få en ny oppfølgingsdato.
        Now.fixedDate(LocalDate.of(2024, 1, 1));

        var startDato = LocalDate.of(2024, 1, 1);
        var sluttDato = LocalDate.of(2025, 6, 1);
        var forrigeVarsel = LocalDate.of(2025, 1, 1);

        var avtaleSlutterIJuni = new Oppfolging(forrigeVarsel, startDato, sluttDato).neste();

        assertNull(avtaleSlutterIJuni.getVarselstidspunkt());
    }

    @Test
    void sisteMuligeFrist() {
        // Vi vil ikke ha oppfølging på samme dag som en avtale avsluttes, men gjerne én dag etter.
        // I praksis kan en avtale dermed ha en siste tilskuddsperiode på én dags varighet, som vil kreve oppfølging.
        Now.fixedDate(LocalDate.of(2024, 1, 1));

        var startdato = LocalDate.of(2024, 1, 1);
        var sluttdatoForTidlig = LocalDate.of(2024, 7, 31);
        var sluttdatoGyldig = LocalDate.of(2024, 8, 1);

        var ingenOppfolging = new Oppfolging(null, startdato, sluttdatoForTidlig).neste();
        var oppfolging = new Oppfolging(null, startdato, sluttdatoGyldig).neste();

        assertNull(ingenOppfolging.getVarselstidspunkt());
        assertEquals(oppfolging.getVarselstidspunkt(), LocalDate.of(2024, 6, 1));
    }

    @Test
    void ingenOppfolgingHvisIngenStartdatoEllerSluttdato() {
        // Dersom man ikke har fylt inn start eller sluttdato på avtalen, så vil det ikke være noen oppfølging
        Now.fixedDate(LocalDate.of(2024, 1, 1));


        var startdato = LocalDate.of(2024, 1, 1);
        var sluttdato = LocalDate.of(2025, 8, 1);

        var ingenOppfolgingPgaAltMangler = new Oppfolging(null, null, null).neste();
        assertNull(ingenOppfolgingPgaAltMangler.getVarselstidspunkt());
        assertNull(ingenOppfolgingPgaAltMangler.getOppfolgingsfrist());

        var oppfolgingPgaSluttdatoMangler = new Oppfolging(null, startdato, null).neste();
        assertNull(oppfolgingPgaSluttdatoMangler.getVarselstidspunkt());
        assertNull(oppfolgingPgaSluttdatoMangler.getOppfolgingsfrist());

        var oppfolgingPgaStartdatoMangler = new Oppfolging(null, null, sluttdato).neste();
        assertNull(oppfolgingPgaStartdatoMangler.getVarselstidspunkt());
        assertNull(oppfolgingPgaStartdatoMangler.getOppfolgingsfrist());


    }
}
