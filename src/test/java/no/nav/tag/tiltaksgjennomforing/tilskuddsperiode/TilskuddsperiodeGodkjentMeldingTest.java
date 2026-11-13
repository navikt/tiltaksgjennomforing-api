package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
class TilskuddsperiodeGodkjentMeldingTest {

    private static final Set<String> FELLES_FELT = Set.of(
        "avtaleId", "tilskuddsperiodeId", "avtaleInnholdId", "avtaleFom", "avtaleTom", "tiltakstype",
        "deltakerFornavn", "deltakerEtternavn", "deltakerFnr", "arbeidsgiverFornavn", "arbeidsgiverEtternavn",
        "arbeidsgiverTlf", "veilederNavIdent", "bedriftNavn", "bedriftNr", "tilskuddsbeløp", "tilskuddFom",
        "tilskuddTom", "avtaleNr", "løpenummer", "resendingsnummer", "enhet", "beslutterNavIdent",
        "veilederFornavn", "veilederEtternavn", "godkjentTidspunkt", "arbeidsgiverKontonummer", "arbeidsgiverKid"
    );
    private static final Set<String> SATSER = Set.of("feriepengerSats", "otpSats", "arbeidsgiveravgiftSats");

    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setup() {
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        new FeatureToggleHolder(featureToggleService);
    }

    @AfterEach
    void tearDown() {
        FeatureToggleHolder.reset();
    }

    @Test
    void lonnstilskudd_sender_satser_og_lonnstilskuddsprosent() {
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();

        JsonNode json = tilJson(avtale, enPeriode());

        assertThat(feltnavn(json)).isEqualTo(union(FELLES_FELT, SATSER, Set.of("lønnstilskuddsprosent")));
        assertThat(json.get("tiltakstype").asString()).isEqualTo("VARIG_LONNSTILSKUDD");
    }

    @Test
    void mentor_sender_satser_og_mentorfelt() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();

        JsonNode json = tilJson(avtale, enPeriode());

        assertThat(feltnavn(json)).isEqualTo(union(FELLES_FELT, SATSER, Set.of("mentorTimelonn", "mentorAntallTimer")));
        assertThat(json.get("mentorTimelonn").isNull()).isFalse();
    }

    @Test
    void vtao_sender_kun_felles_felt_selv_om_avtalen_har_satser() {
        Avtale avtale = TestData.enVtaoArenaAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setOtpSats(0.02);

        JsonNode json = tilJson(avtale, enPeriode());

        assertThat(feltnavn(json)).isEqualTo(FELLES_FELT);
    }

    @Test
    void inkluderingstilskudd_sender_utgiften_som_er_knyttet_til_perioden() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        TilskuddPeriode periode = enPeriode();
        Inkluderingstilskuddsutgift utgift = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().getFirst();
        utgift.setTilskuddperiode(periode);

        JsonNode json = tilJson(avtale, periode);

        assertThat(feltnavn(json)).isEqualTo(union(FELLES_FELT, Set.of("inkluderingstilskuddsutgift")));
        JsonNode utgiftJson = json.get("inkluderingstilskuddsutgift");
        assertThat(utgiftJson.get("beløp").asInt()).isEqualTo(utgift.getBeløp());
        assertThat(utgiftJson.get("type").asString()).isEqualTo(utgift.getType().name());
    }

    @Test
    void inkluderingstilskudd_uten_utgift_for_perioden_sendes_uten_utgift() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();

        JsonNode json = tilJson(avtale, enPeriode());

        assertThat(json.get("tiltakstype").asString()).isEqualTo("INKLUDERINGSTILSKUDD");
        assertThat(json.get("inkluderingstilskuddsutgift").isNull()).isTrue();
    }

    @Test
    void arbeidstrening_feiler() {
        Avtale avtale = TestData.enArbeidstreningAvtale();

        assertThatThrownBy(() -> TilskuddsperiodeGodkjentMelding.create(avtale, enPeriode(), null))
            .isInstanceOf(IllegalStateException.class);
    }

    private TilskuddPeriode enPeriode() {
        TilskuddPeriode periode = new TilskuddPeriode(10000, Now.localDate(), Now.localDate().plusMonths(1));
        periode.setLøpenummer(1);
        return periode;
    }

    private JsonNode tilJson(Avtale avtale, TilskuddPeriode periode) {
        return mapper.readTree(mapper.writeValueAsString(TilskuddsperiodeGodkjentMelding.create(avtale, periode, null)));
    }

    private static Set<String> feltnavn(JsonNode json) {
        return json.propertyNames().stream().collect(Collectors.toSet());
    }

    @SafeVarargs
    private static Set<String> union(Set<String>... sett) {
        return java.util.Arrays.stream(sett).flatMap(Set::stream).collect(Collectors.toSet());
    }
}
