package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FnrMaskerTest {
    private final FnrMasker masker = new FnrMasker();

    @Test
    public void testFnrMaskering() {
        assertEquals("""
                        Exception thrown when sending a message with key='e123aszc-6acd-4213-azxe-4123123123' and payload='{"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr":"1234*******","mentor...' to topic arbeidsgiver.tiltak-avtale-hendelse:
                        """, masker.mask(null, """
                        Exception thrown when sending a message with key='e123aszc-6acd-4213-azxe-4123123123' and payload='{"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr"   :   "12345678911","mentor...' to topic arbeidsgiver.tiltak-avtale-hendelse:
                        """),
                "Maskerer fødselsnumre som dukker opp i logger, med X antall mellomrom i json-objektet");

        assertEquals("""
                        Exception thrown when sending a message with key='e123aszc-6acd-4213-azxe-4123123123' and payload='{"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr":"1234*******","mentor...' to topic arbeidsgiver.tiltak-avtale-hendelse:
                        """, masker.mask(null, """
                        Exception thrown when sending a message with key='e123aszc-6acd-4213-azxe-4123123123' and payload='{"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr":"12345678911","mentor...' to topic arbeidsgiver.tiltak-avtale-hendelse:
                        """),
                "Maskerer fødselsnumre som dukker opp i logger");
    }

    @Test
    public void testDelvisFnrMaskering() {
        assertEquals("""
                        Exception thrown when sending a message with key='046123sdc-332h-5123-bc42-asdacz2123as' and payload='{"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr":"1234**...' to topic arbeidsgiver.tiltak-avtale-hendelse:
                        """, masker.mask(null, """
                        Exception thrown when sending a message with key='046123sdc-332h-5123-bc42-asdacz2123as' and payload='{"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr":"123456...' to topic arbeidsgiver.tiltak-avtale-hendelse:
                        """),
                "Maskerer fødselsnumre som kun delvis dukker opp i logger");
    }

    @Test
    public void testIngenMaskeringHvisMindreEnn4Siffer() {
        assertEquals("""
                        "deltakerFnr":"123...
                        """, masker.mask(null, """
                        "deltakerFnr"   : "123...
                        """),
                "Hvis et delvis logget fødselsnummer er mindre enn 4 siffer, så gjør vi ingen maskering");
    }
}
