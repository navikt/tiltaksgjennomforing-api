package no.nav.tag.tiltaksgjennomforing.enhet;

import org.junit.jupiter.api.Test;

import static no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe.*;
import static org.junit.jupiter.api.Assertions.*;

class InnsatsgruppeTest {

    @Test
    void is_equal_returnerer_true_naar_begge_er_null() {
        assertTrue(isArenaOboEqual(null, null));
    }

    @Test
    void is_equal_returnerer_true_naar_begge_er_samme_verdi() {
        Innsatsgruppe innsatsgruppeArena = TRENGER_VEILEDNING;
        Innsatsgruppe innsatsgruppeObo = TRENGER_VEILEDNING;
        assertTrue(isArenaOboEqual(innsatsgruppeArena, innsatsgruppeObo));
    }

    @Test
    void is_equal_returnerer_true_naar_arena_er_ukjent_og_obo_er_null() {
        Innsatsgruppe innsatsgruppeArena = UKJENT;
        Innsatsgruppe innsatsgruppeObo = null;
        assertTrue(isArenaOboEqual(innsatsgruppeArena, innsatsgruppeObo));
    }

    @Test
    void is_equal_returnerer_true_naar_begge_er_variant_av_varig_tilpasset() {
        assertTrue(isArenaOboEqual(JOBBE_DELVIS, LITEN_MULIGHET_TIL_A_JOBBE));
        assertTrue(isArenaOboEqual(LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS));
        assertTrue(isArenaOboEqual(JOBBE_DELVIS, JOBBE_DELVIS));
        assertTrue(isArenaOboEqual(LITEN_MULIGHET_TIL_A_JOBBE, LITEN_MULIGHET_TIL_A_JOBBE));
    }

    @Test
    void is_equal_returnerer_false_naar_ulik_vanlig_innsatsgruppe() {
        assertFalse(isArenaOboEqual(TRENGER_VEILEDNING, GODE_MULIGHETER));
        assertFalse(isArenaOboEqual(GODE_MULIGHETER, TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE));
    }

    @Test
    void is_equal_returnerer_false_naar_arena_ikke_er_ukjent_og_obo_er_null() {
        Innsatsgruppe innsatsgruppeArena = TRENGER_VEILEDNING;
        Innsatsgruppe innsatsgruppeObo = null;
        assertFalse(isArenaOboEqual(innsatsgruppeArena, innsatsgruppeObo));
    }

    @Test
    void is_equal_returnerer_false_naar_obo_er_null_og_arena_er_variant_av_varig_tilpasset() {
        assertFalse(isArenaOboEqual(LITEN_MULIGHET_TIL_A_JOBBE, null));
        assertFalse(isArenaOboEqual(JOBBE_DELVIS, null));
    }

    @Test
    void is_equal_returnerer_false_naar_kun_en_er_variant_av_varig_tilpasset() {
        assertFalse(isArenaOboEqual(JOBBE_DELVIS, TRENGER_VEILEDNING));
        assertFalse(isArenaOboEqual(TRENGER_VEILEDNING, LITEN_MULIGHET_TIL_A_JOBBE));
    }

    @Test
    void is_equal_returnerer_false_naar_obo_er_ukjent() {
        assertFalse(isArenaOboEqual(TRENGER_VEILEDNING, UKJENT));
        assertFalse(isArenaOboEqual(UKJENT, UKJENT));
        assertFalse(isArenaOboEqual(null, UKJENT));
    }
}
