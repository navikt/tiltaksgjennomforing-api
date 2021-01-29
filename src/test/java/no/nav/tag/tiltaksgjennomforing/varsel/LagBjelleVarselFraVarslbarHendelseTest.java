package no.nav.tag.tiltaksgjennomforing.varsel;

import static no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LagBjelleVarselFraVarslbarHendelseTest {
    private static Avtale avtale;
    private static Identifikator deltaker;
    private static Identifikator arbeidsgiver;
    private static Identifikator veileder;
    private static TilskuddPeriode gjeldendeperiode;

    @BeforeAll
    static void setUp() {
        avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        deltaker = avtale.getDeltakerFnr();
        arbeidsgiver = avtale.getBedriftNr();
        veileder = avtale.getVeilederNavIdent();
        gjeldendeperiode = avtale.gjeldendeTilskuddsperiode();
        avtale.avslåTilskuddsperiode(TestData.enNavIdent(), EnumSet.of(Avslagsårsak.FEIL_I_REGELFORSTÅELSE), "registrert feil i fakta");

    }

    @DisplayName("Skal varsle riktig mottakere når hendelse oppstår")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provider")
    void testLagBjelleVarsler(VarslbarHendelseType hendelse, GamleVerdier gamleVerdier, List<Identifikator> skalVarsles) {

        List<BjelleVarsel> bjelleVarsler = LagBjelleVarselFraVarslbarHendelse.lagBjelleVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, hendelse), gamleVerdier);
        assertThat(bjelleVarsler).extracting("identifikator").containsOnlyElementsOf(skalVarsles);
        if (!bjelleVarsler.isEmpty()) {
             assertThat(bjelleVarsler).extracting("varslbarHendelseType").containsOnly(hendelse);
        }
    }

    private static Stream<Arguments> provider() {
        return Stream.of(
                of(TILSKUDDSPERIODE_AVSLATT, new GamleVerdier(), List.of(veileder)),
                of(OPPRETTET, new GamleVerdier(), List.of(deltaker, arbeidsgiver)),
                of(ENDRET, new GamleVerdier(), List.of()),
                of(GODKJENT_AV_DELTAKER, new GamleVerdier(), List.of(veileder)),
                of(GODKJENT_AV_ARBEIDSGIVER, new GamleVerdier(), List.of(veileder)),
                of(GODKJENT_AV_VEILEDER, new GamleVerdier(), List.of(deltaker, arbeidsgiver)),
                of(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, new GamleVerdier(true, false), List.of(deltaker, veileder)),
                of(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, new GamleVerdier(false, false), List.of(veileder)),
                of(GODKJENNINGER_OPPHEVET_AV_VEILEDER, new GamleVerdier(true, false), List.of(deltaker)),
                of(GODKJENNINGER_OPPHEVET_AV_VEILEDER, new GamleVerdier(false, true), List.of(arbeidsgiver)),
                of(GODKJENNINGER_OPPHEVET_AV_VEILEDER, new GamleVerdier(true, true), List.of(deltaker, arbeidsgiver)),
                of(DELT_MED_ARBEIDSGIVER, new GamleVerdier(), List.of()),
                of(DELT_MED_DELTAKER, new GamleVerdier(), List.of())
        );
    }
}
