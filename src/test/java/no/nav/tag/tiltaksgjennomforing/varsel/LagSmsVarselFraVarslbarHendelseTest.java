package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class LagSmsVarselFraVarslbarHendelseTest {
    private static Avtale avtale;
    private static Tuple deltaker;
    private static Tuple arbeidsgiver;
    private static Tuple veileder;

    @BeforeAll
    static void setUp() {
        avtale = TestData.enArbeidstreningAvtale();
        deltaker = tuple(
                avtale.getDeltakerTlf(),
                avtale.getDeltakerFnr(),
                "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing");
        arbeidsgiver = tuple(
                avtale.getArbeidsgiverTlf(),
                avtale.getBedriftNr(),
                "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing");
        veileder = tuple(
                avtale.getVeilederTlf(),
                SmsVarselFactory.NAV_ORGNR,
                "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing");
    }

    @DisplayName("Skal varsle riktig mottakere når hendelse oppstår")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provider")
    void testLagSmsVarsler(VarslbarHendelseType hendelse, GamleVerdier gamleVerdier, List<Tuple> skalVarsles) {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, hendelse), gamleVerdier);
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .containsOnlyElementsOf(skalVarsles);
    }

    private static Stream<Arguments> provider() {
        return Stream.of(
                Arguments.of(VarslbarHendelseType.OPPRETTET, new GamleVerdier(), List.of()),
                Arguments.of(VarslbarHendelseType.ENDRET, new GamleVerdier(), List.of()),
                Arguments.of(VarslbarHendelseType.GODKJENT_AV_DELTAKER, new GamleVerdier(), List.of(veileder)),
                Arguments.of(VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER, new GamleVerdier(), List.of(veileder)),
                Arguments.of(VarslbarHendelseType.GODKJENT_AV_VEILEDER, new GamleVerdier(), List.of(deltaker, arbeidsgiver)),
                Arguments.of(VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, new GamleVerdier(true, false), List.of(deltaker, veileder)),
                Arguments.of(VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, new GamleVerdier(false, false), List.of(veileder)),
                Arguments.of(VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, new GamleVerdier(true, false), List.of(deltaker)),
                Arguments.of(VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, new GamleVerdier(false, true), List.of(arbeidsgiver)),
                Arguments.of(VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, new GamleVerdier(true, true), List.of(deltaker, arbeidsgiver)),
                Arguments.of(VarslbarHendelseType.DELT_MED_ARBEIDSGIVER, new GamleVerdier(), List.of(arbeidsgiver)),
                Arguments.of(VarslbarHendelseType.DELT_MED_DELTAKER, new GamleVerdier(), List.of(deltaker))
        );
    }
}