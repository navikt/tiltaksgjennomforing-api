package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BeslutterTest {

    @Test
    public void toggle_godkjent_for_etterregistrering() {

        //GITT
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 12, 12));
        endreAvtale.setSluttDato(LocalDate.of(2021, 12, 1).plusYears(1));
        Beslutter beslutter = TestData.enBeslutter(avtale);

        // NÅR
        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        assertThat(avtale.isGodkjentForEtterregistrering()).isTrue();

        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        assertThat(avtale.isGodkjentForEtterregistrering()).isFalse();
    }

    @Test
    public void kan_ikke_godkjenne_periode_på_enhet_som_ikke_finnes() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                featureToggleServiceMock,
                mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(eq(veileder), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);

        avtale.endreAvtale(
                TestData.endringPåAlleLønnstilskuddFelter(),
                Avtalerolle.VEILEDER
        );
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        assertThat(avtale.erAvtaleInngått()).isFalse();
        Beslutter beslutter = TestData.enBeslutter(avtale);
        assertFeilkode(
                Feilkode.ENHET_FINNES_IKKE,
                () -> beslutter.godkjennTilskuddsperiode(avtale, "9999")
        );
        assertThat(avtale.erAvtaleInngått()).isFalse();
    }
}
