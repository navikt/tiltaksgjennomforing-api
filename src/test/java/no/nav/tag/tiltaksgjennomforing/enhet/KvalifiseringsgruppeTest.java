package no.nav.tag.tiltaksgjennomforing.enhet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KvalifiseringsgruppeTest {

    @Test
    void parse() {
        assertEquals(
            Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
            Kvalifiseringsgruppe.parse(Kvalifiseringsgruppe.GRADERT_VARIG_TILPASSET_INNSATS.getKvalifiseringskode())
        );
        assertEquals(
            Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
            Kvalifiseringsgruppe.parse(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode())
        );
    }
}
