package no.nav.tag.tiltaksgjennomforing.satser;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SatsTest {

    @Test
    void henterUtBelopTest() {
        var sats = new Sats("VTAO", List.of(
                new SatserEntitet(
                        "VTAO",
                        6000.0,
                        LocalDate.of(2021, 1, 1),
                        LocalDate.of(2021, 12, 31)),
                new SatserEntitet(
                        "VTAO",
                        6500.0,
                        LocalDate.of(2022, 1, 1),
                        LocalDate.of(2022, 12, 31)),
                new SatserEntitet(
                        "VTAO",
                        8000.0,
                        LocalDate.of(2024, 1, 1),
                        null
                )));
        assertEquals(
                6500.0,
                sats.hentGjeldendeSats(LocalDate.of(2022, 4, 1)),
                "Henter riktig sats innenfor perioden");
        assertEquals(
                6500.0,
                sats.hentGjeldendeSats(LocalDate.of(2022, 1, 1)),
                "Henter riktig sats på starten av perioden");
        assertEquals(
                6500.0,
                sats.hentGjeldendeSats(LocalDate.of(2022, 12, 31)),
                "Henter sats for enden av perioden");
        assertEquals(
                8000.0,
                sats.hentGjeldendeSats(LocalDate.of(4000, 12, 31)),
                "Henter sats for åpen periode");
        assertNull(
                sats.hentGjeldendeSats(LocalDate.of(1000, 12, 31)),
                "Henter sats før kjente perioder"
        );
        assertNull(
                sats.hentGjeldendeSats(LocalDate.of(2023, 12, 31)),
                "Kan ikke hente sats for en periode som ikke fins"
        );
    }
}
