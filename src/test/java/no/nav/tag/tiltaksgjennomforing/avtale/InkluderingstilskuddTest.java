package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class InkluderingstilskuddTest {

    @Test
    public void endreInkluderingstilskudd_verifisere_enkel_endring() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.endreAvtale(TestData.endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER);
    }

    @Test
    public void endreInkluderingstilskudd_verifisere_endring_etter_godkjenning() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver().getIdentifikator());
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        List<Inkluderingstilskuddsutgift> eksisterendeUtgifter = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift();

        // Jukse litt ved å sette id på inkluderingstilskudd-entitetene, ettersom denne testen ikke lagrer til database,
        // og endreInkluderingstilskudd-metoden forventer at id er satt for eksisterende utgifter.
        eksisterendeUtgifter.forEach(x -> x.setId(UUID.randomUUID()));

        EndreInkluderingstilskudd endreInkluderingstilskudd = TestData.endringMedNyeInkluderingstilskudd(eksisterendeUtgifter);
        avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, TestData.enNavIdent());
    }
}
