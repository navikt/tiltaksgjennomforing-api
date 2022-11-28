package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

public class InkluderingstilskuddTest {

    @Test
    public void endreInkluderingstilskudd_verifisere_enkel_endring() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.endreAvtale(avtale.getSistEndret(), TestData.endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of(), List.of());
    }

    @Test
    public void endreInkluderingstilskudd_verifisere_endring_etter_godkjenning() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver().getIdentifikator());
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn(), List.of(), List.of());
        List<Inkluderingstilskuddsutgift> eksisterendeUtgifter = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift();
        EndreInkluderingstilskudd endreInkluderingstilskudd = TestData.endringMedNyeInkluderingstilskudd(eksisterendeUtgifter);
        avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, TestData.enNavIdent());
    }
}
