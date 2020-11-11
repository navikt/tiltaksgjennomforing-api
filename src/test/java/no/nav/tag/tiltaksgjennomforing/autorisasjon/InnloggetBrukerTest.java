package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.*;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class InnloggetBrukerTest {

    private Fnr deltaker;
    private NavIdent navIdent;
    private Avtale avtale;
    private BedriftNr bedriftNr;
    private TilgangskontrollService tilgangskontrollService;

    @Before
    public void setup() {
        deltaker = new Fnr("10000000000");
        navIdent = new NavIdent("X100000");
        bedriftNr = new BedriftNr("12345678901");
        avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltaker, bedriftNr, Tiltakstype.ARBEIDSTRENING), navIdent);
        tilgangskontrollService = mock(TilgangskontrollService.class);
    }

    @Test
    public void deltakerKnyttetTilAvtaleSkalHaDeltakerRolle() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        InnloggetDeltaker selvbetjeningBruker = TestData.innloggetDeltaker(TestData.enDeltaker(avtale));
        assertThat(selvbetjeningBruker.avtalepart(avtale)).isInstanceOf(Deltaker.class);
    }

    @Test
    public void arbeidsgiverKnyttetTilAvtaleSkalHaArbeidsgiverRolle() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        InnloggetArbeidsgiver selvbetjeningBruker = TestData.innloggetArbeidsgiver(TestData.enArbeidsgiver(avtale));
        assertThat(selvbetjeningBruker.avtalepart(avtale)).isInstanceOf(Arbeidsgiver.class);
    }

    @Test
    public void veilederKnyttetTilAvtaleSkalHaVeilederRolle() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        InnloggetVeileder navAnsatt = new InnloggetVeileder(avtale.getVeilederNavIdent(), tilgangskontrollService);
        assertThat(navAnsatt.avtalepart(avtale)).isInstanceOf(Veileder.class);
    }

    @Test
    public void harTilgang__deltaker_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetDeltaker(deltaker).harLeseTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        InnloggetVeileder innloggetVeileder = new InnloggetVeileder(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harLesetilgangTilKandidat(innloggetVeileder, avtale.getDeltakerFnr())).thenReturn(true);

        assertThat(innloggetVeileder.harLeseTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).harLesetilgangTilKandidat(innloggetVeileder, avtale.getDeltakerFnr());
    }

    @Test
    public void harTilgang__veileder_skal_ikke_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        InnloggetVeileder innloggetVeileder = new InnloggetVeileder(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harLesetilgangTilKandidat(innloggetVeileder, avtale.getDeltakerFnr())).thenReturn(false);

        assertThat(innloggetVeileder.harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        InnloggetVeileder innloggetVeileder = new InnloggetVeileder(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(innloggetVeileder, avtale.getDeltakerFnr())).thenReturn(true);

        assertThat(innloggetVeileder.harSkriveTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).harSkrivetilgangTilKandidat(innloggetVeileder, avtale.getDeltakerFnr());
    }

    @Test
    public void harTilgang__veileder_skal_ikke_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        InnloggetVeileder innloggetVeileder = new InnloggetVeileder(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(innloggetVeileder, avtale.getDeltakerFnr())).thenReturn(false);

        assertThat(innloggetVeileder.harSkriveTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avtale() {
        assertThat(new InnloggetArbeidsgiver(TestData.etFodselsnummer(), Map.of(), Collections.emptySet()).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_lesetilgang_hvis_toggle_er_av() {
        assertThat(new InnloggetVeileder(new NavIdent("X123456"), tilgangskontrollService).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_skrivetilgang_hvis_toggle_er_av() {
        assertThat(new InnloggetVeileder(new NavIdent("X123456"), tilgangskontrollService).harSkriveTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetArbeidsgiver(new Fnr("00000000001"), Map.of(), Collections.emptySet()).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(this.bedriftNr, Set.of(Tiltakstype.values()));
        InnloggetArbeidsgiver innloggetArbeidsgiver = new InnloggetArbeidsgiver(new Fnr("00000000009"), tilganger, Collections.emptySet());
        assertThat(innloggetArbeidsgiver.harLeseTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avbrutt_avtale_eldre_enn_12_uker() {
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(this.bedriftNr, Set.of(Tiltakstype.values()));
        InnloggetArbeidsgiver innloggetArbeidsgiver = new InnloggetArbeidsgiver(new Fnr("00000000009"), tilganger, Collections.emptySet());
        avtale.setAvbrutt(true);
        avtale.setSistEndret(Instant.now().minus(84, ChronoUnit.DAYS).minusMillis(100));
        assertThat(innloggetArbeidsgiver.harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setSluttDato(LocalDate.now().minusDays(85));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.values()));
        InnloggetArbeidsgiver innloggetArbeidsgiver = new InnloggetArbeidsgiver(new Fnr("00000000009"), tilganger, Collections.emptySet());
        assertThat(innloggetArbeidsgiver.harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker_når_ikke_godkjent_av_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setSluttDato(LocalDate.now().minusDays(85));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.values()));
        InnloggetArbeidsgiver innloggetArbeidsgiver = new InnloggetArbeidsgiver(new Fnr("00000000009"), tilganger, Collections.emptySet());
        assertThat(innloggetArbeidsgiver.harLeseTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__arbeidsgiver_med_arbeidsgivertilgang_skal_ikke_ha_lonnstilskuddtilgang() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING));
        InnloggetArbeidsgiver innloggetArbeidsgiver = new InnloggetArbeidsgiver(new Fnr("00000000009"), tilganger, Collections.emptySet());
        assertThat(innloggetArbeidsgiver.harLeseTilgang(avtale)).isFalse();
    }
}
