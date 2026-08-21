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

    @Test
    void is_equal_to_innsatsgruppe_returnerer_false_for_null() {
        assertFalse(Kvalifiseringsgruppe.STANDARD_INNSATS.isEqualToInnsatsgruppe(null));
        assertFalse(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.isEqualToInnsatsgruppe(null));
    }

    @Test
    void is_equal_to_innsatsgruppe_matcher_egen_innsatsgruppe() {
        assertTrue(Kvalifiseringsgruppe.STANDARD_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.GODE_MULIGHETER));
        assertTrue(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.TRENGER_VEILEDNING));
        assertTrue(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE));
        assertTrue(Kvalifiseringsgruppe.GRADERT_VARIG_TILPASSET_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.JOBBE_DELVIS));
    }

    @Test
    void is_equal_to_innsatsgruppe_matcher_ikke_annen_innsatsgruppe() {
        assertFalse(Kvalifiseringsgruppe.STANDARD_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.TRENGER_VEILEDNING));
        assertFalse(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.GODE_MULIGHETER));
        assertFalse(Kvalifiseringsgruppe.GRADERT_VARIG_TILPASSET_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.LITEN_MULIGHET_TIL_A_JOBBE));
    }

    @Test
    void is_equal_to_innsatsgruppe_varig_tilpasset_matcher_begge_varige_varianter() {
        assertTrue(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.LITEN_MULIGHET_TIL_A_JOBBE));
        assertTrue(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.isEqualToInnsatsgruppe(Innsatsgruppe.JOBBE_DELVIS));
    }

    @Test
    void is_equal_to_innsatsgruppe_ukjent_matcher_kun_ukjent() {
        assertTrue(Kvalifiseringsgruppe.IKKE_VURDERT.isEqualToInnsatsgruppe(Innsatsgruppe.UKJENT));
        assertFalse(Kvalifiseringsgruppe.IKKE_VURDERT.isEqualToInnsatsgruppe(Innsatsgruppe.GODE_MULIGHETER));
    }
}
