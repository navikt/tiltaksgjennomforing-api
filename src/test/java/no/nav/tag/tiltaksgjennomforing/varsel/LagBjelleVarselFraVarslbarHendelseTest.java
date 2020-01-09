package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class LagBjelleVarselFraVarslbarHendelseTest {
    private static Avtale avtale;
    private static Identifikator deltaker;
    private static Identifikator arbeidsgiver;
    private static Identifikator veileder;

    @BeforeAll
    static void setUp() {
        avtale = TestData.enAvtale();
        deltaker = avtale.getDeltakerFnr();
        arbeidsgiver = avtale.getBedriftNr();
        veileder = avtale.getVeilederNavIdent();
    }

    @DisplayName("Skal varsle riktig mottakere når hendelse oppstår")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provider")
    void testLagBjelleVarsler(VarslbarHendelseType hendelse, GamleVerdier gamleVerdier, List<Identifikator> skalVarsles) {
        Avtale avtale = TestData.enAvtale();
        List<BjelleVarsel> bjelleVarsler = LagBjelleVarselFraVarslbarHendelse.lagBjelleVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, hendelse), gamleVerdier);
        assertThat(bjelleVarsler).extracting("identifikator").containsOnlyElementsOf(skalVarsles);
        if (!bjelleVarsler.isEmpty()) {
            assertThat(bjelleVarsler).extracting("varslingstekst").containsOnly(hendelse.getTekst());
        }
    }

    private static Stream<Arguments> provider() {
        return Stream.of(
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