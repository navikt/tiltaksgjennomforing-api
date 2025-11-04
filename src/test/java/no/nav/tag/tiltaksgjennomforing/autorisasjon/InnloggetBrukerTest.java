package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Deltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Mentor;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorTilskuddsperioderToggle;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.Regelmotor;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InnloggetBrukerTest {

    private Fnr deltaker;
    private NavIdent navIdent;
    private Avtale avtale;
    private BedriftNr bedriftNr;
    private TilgangskontrollService tilgangskontrollService;
    private KontoregisterService kontoregisterService;
    private PersondataService persondataService;
    private Norg2Client norg2Client;
    private VeilarboppfolgingService veilarboppfolgingService;
    private AvtaleRepository avtaleRepository;
    private FeatureToggleService featureToggleService;
    @Value("${tiltaksgjennomforing.mentor-tilskuddsperioder.enabled}")
    private boolean mentorTilskuddsperioderEnabled;

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;

        deltaker = new Fnr("00000000000");
        navIdent = new NavIdent("X100000");
        bedriftNr = new BedriftNr("12345678901");
        avtale = Avtale.opprett(new OpprettAvtale(deltaker, bedriftNr, Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, navIdent);
        tilgangskontrollService = mock(TilgangskontrollService.class);
        persondataService = mock(PersondataService.class);
        kontoregisterService = mock(KontoregisterService.class);
        veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        avtaleRepository = mock(AvtaleRepository.class);

        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void harTilgang__deltaker_skal_ha_tilgang_til_avtale() {
        assertThat(new Deltaker(deltaker).harTilgangTilAvtale(avtale).erTillat()).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService,
                mock(EregService.class),mock(Regelmotor.class)
        );

        assertThat(veileder.harTilgangTilAvtale(avtale).erTillat()).isTrue();
        verify(tilgangskontrollService).hentSkrivetilgang(veileder, avtale.getDeltakerFnr());
    }

    @Test
    public void harTilgang__veileder_skal_ikke_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC,"Ikke tilgang fra ABAC"));
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService,
                mock(EregService.class),mock(Regelmotor.class)
        );
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                veileder,
                avtale.getDeltakerFnr()
        )).thenReturn(false);

        assertThat(veileder.harTilgangTilAvtale(avtale).erTillat()).isFalse();
    }

    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService,
                mock(EregService.class),mock(Regelmotor.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        assertThat(veileder.harTilgangTilAvtale(avtale).erTillat()).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ikke_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService,
                mock(EregService.class),mock(Regelmotor.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(
                veileder,
                avtale.getDeltakerFnr())
        ).thenReturn(new Tilgang.Avvis(Avslagskode.EKSTERN_BRUKER_HAR_IKKE_TILGANG, "Ekstern bruker har ikke tilgang"));

        assertThat(veileder.harTilgangTilAvtale(avtale).erTillat()).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avtale() {
        assertThat(
                new Arbeidsgiver(
                        TestData.etFodselsnummer(),
                        Set.of(),
                        Map.of(),
                        List.of(),
                        persondataService,
                        null,
                        null,
                        null,
                    null
                ).harTilgangTilAvtale(avtale).erTillat()
        ).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_lesetilgang_hvis_toggle_er_av() {
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC,"Ikke tilgang fra ABAC"));

        assertThat(
                new Veileder(
                        new NavIdent("X123456"),
                        null,
                        tilgangskontrollService,
                        persondataService,
                        norg2Client,
                        Collections.emptySet(),
                        TestData.INGEN_AD_GRUPPER,
                        veilarboppfolgingService,
                        featureToggleService,
                        mock(EregService.class),mock(Regelmotor.class)
                ).harTilgangTilAvtale(avtale).erTillat()
        ).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_skrivetilgang_hvis_toggle_er_av() {

        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC,"Ikke tilgang fra ABAC"));
        assertThat(
                new Veileder(
                        new NavIdent("X123456"),
                        null,
                        tilgangskontrollService,
                        persondataService,
                        norg2Client,
                        Collections.emptySet(),
                        TestData.INGEN_AD_GRUPPER,
                        veilarboppfolgingService,
                        featureToggleService,
                        mock(EregService.class),mock(Regelmotor.class)
                ).harTilgangTilAvtale(avtale).erTillat()
        ).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC,"Ikke tilgang fra ABAC"));
        assertThat(
            new Arbeidsgiver(
                Fnr.generer(1956, 7, 8),
                Set.of(),
                Map.of(),
                List.of(),
                persondataService,
                null,
                null,
                null,null
            ).harTilgangTilAvtale(avtale).erTillat()
        ).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC,"Ikke tilgang fra ABAC"));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(this.bedriftNr, Set.of(Tiltakstype.values()));
        Arbeidsgiver Arbeidsgiver = new Arbeidsgiver(
                Fnr.generer(1956, 7, 8),
                Set.of(),
                tilganger,
                List.of(),
                persondataService,
                null,
                null,
                null,null
        );
        assertThat(Arbeidsgiver.harTilgangTilAvtale(avtale).erTillat()).isTrue();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusDays(85));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.values()));
        Arbeidsgiver Arbeidsgiver = new Arbeidsgiver(
            Fnr.generer(1956, 7, 8),
                Set.of(),
                tilganger,
                List.of(),
                persondataService,
                null,
                null,
                null,null
        );
        assertThat(Arbeidsgiver.harTilgangTilAvtale(avtale).erTillat()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void harTilgang__mentor_uten_tilskuddsperioder_skal_ikke_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker(boolean mentorTilskuddsperioderEnabled) {
        MentorTilskuddsperioderToggle.setValue(mentorTilskuddsperioderEnabled);

        Avtale avtale = TestData.enMentorAvtaleSignert();
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusDays(85));
        Mentor mentor = new Mentor(avtale.getMentorFnr());
        assertThat(mentor.harTilgangTilAvtale(avtale).erTillat()).isFalse();
    }

    @Test
    public void harTilgang__mentor_skal_ikke_ha_tilgang_til_en_annullert_avtale_eldre_enn_12_uker() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        avtale.setAnnullertTidspunkt(Instant.now().minus(85, ChronoUnit.DAYS));
        Mentor mentor = new Mentor(avtale.getMentorFnr());
        assertThat(mentor.harTilgangTilAvtale(avtale).erTillat()).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker_når_ikke_godkjent_av_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusDays(85));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.values()));
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                Fnr.generer(1956, 7, 8),
                Set.of(),
                tilganger,
                List.of(),
                persondataService,
                null,
                null,
                null,null
        );

        assertThat(arbeidsgiver.harTilgangTilAvtale(avtale).erTillat()).isTrue();
    }

    @Test
    public void harTilgang__arbeidsgiver_med_arbeidsgivertilgang_skal_ikke_ha_lonnstilskuddtilgang() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING));
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                Fnr.generer(1956, 7, 8),
                Set.of(),
                tilganger,
                List.of(),
                persondataService,
                null,
                null,
                null,null
        );
        assertThat(arbeidsgiver.harTilgangTilAvtale(avtale).erTillat()).isFalse();
    }
}
