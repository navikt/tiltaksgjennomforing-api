package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.jsonHarNøkkel;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.jsonHarVerdi;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvtaleApiTestUtilTest {
    @Test
    public void jsonValueTest() {
        assertTrue(jsonHarVerdi("""
                          [1,
                           2,
                           {"a": 1,
                           "b": {"b1": [{"deltakerFnr": "12345678901"}]},
                           "c": 3}]
                        """, "12345678901"),
                "Finner verdier i nøstet data som starter med en liste");
        assertTrue(jsonHarVerdi("""
                          {"a": 1,
                           "b": {"b1": [{"deltakerFnr": "12345678901"}]},
                           "c": 3}
                        """, "12345678901"),
                "Finner verdier i nøstet data som starter med en map");

        assertTrue(jsonHarVerdi("""
                          {"a": 1,
                           "b": {"b1": [1, 2, 3, "12345678901"]},
                           "c": 3}
                        """, "12345678901"),
                "Finner verdier i nøstet data som starter med en map i en liste");

        assertFalse(jsonHarVerdi("""
                          {"a": 1,
                           "b": {"b1": [1, 2, 3, "12345678901"]},
                           "c": 3}
                        """, "b1"),
                "Finner ikke 'verdier' som egentlig er nøkler");
    }

    @Test
    public void jsonKeysTest() {
        assertTrue(jsonHarNøkkel("""
                          [1,
                           2,
                           {"a": 1,
                           "b": {"b1": [{"deltakerFnr": "12345678901"}]},
                           "c": 3}]
                        """, "b1"),
                "Finner nøkler i nøstet data som starter med en liste");
        assertTrue(jsonHarNøkkel("""
                          {"a": 1,
                           "b": {"b1": [{"deltakerFnr": "12345678901"}]},
                           "c": 3}
                        """, "b1"),
                "Finner nøkler i nøstet data som starter med en map");

        assertTrue(jsonHarNøkkel("""
                          {"a": 1,
                           "b": {"b1": [1, 2, 3, {"b2": "12345678901"}]},
                           "c": 3}
                        """, "b2"),
                "Finner nøkler i nøstet data som starter med en map i en liste");

        assertFalse(jsonHarNøkkel("""
                          {"a": 1,
                           "b": {"b1": [1, 2, 3, "12345678901"]},
                           "c": 3}
                        """, "12345678901"),
                "Finner ikke 'nøkler' som egentlig er verdier");
    }
}
