package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Deltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.Before;
import org.junit.Test;

public class InnloggetBrukerTest {

    private Fnr deltaker;
    private NavIdent navIdent;
    private Avtale avtale;
    private BedriftNr bedriftNr;
    private TilgangskontrollService tilgangskontrollService;
    private PersondataService persondataService;
    private Norg2Client norg2Client;

    @Before
    public void setup() {
        deltaker = new Fnr("10000000000");
        navIdent = new NavIdent("X100000");
        bedriftNr = new BedriftNr("12345678901");
        avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltaker, bedriftNr, Tiltakstype.ARBEIDSTRENING), navIdent);
        tilgangskontrollService = mock(TilgangskontrollService.class);
        persondataService = mock(PersondataService.class);
    }

    @Test
    public void harTilgang__deltaker_skal_ha_tilgang_til_avtale() {
        assertThat(new Deltaker(deltaker).harTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(veileder.getIdentifikator(), avtale.getDeltakerFnr())).thenReturn(true);

        assertThat(veileder.harTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).harSkrivetilgangTilKandidat(veileder.getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Test
    public void harTilgang__veileder_skal_ikke_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(veileder.getIdentifikator(), avtale.getDeltakerFnr())).thenReturn(false);

        assertThat(veileder.harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(veileder.getIdentifikator(), avtale.getDeltakerFnr())).thenReturn(true);

        assertThat(veileder.harTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).harSkrivetilgangTilKandidat(veileder.getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Test
    public void harTilgang__veileder_skal_ikke_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(veileder.getIdentifikator(), avtale.getDeltakerFnr())).thenReturn(false);

        assertThat(veileder.harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avtale() {
        assertThat(new Arbeidsgiver(TestData.etFodselsnummer(), Set.of(), Map.of(), null, null).harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_lesetilgang_hvis_toggle_er_av() {
        assertThat(
            new Veileder(new NavIdent("X123456"), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet()).harTilgang(avtale))
            .isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_skrivetilgang_hvis_toggle_er_av() {
        assertThat(
            new Veileder(new NavIdent("X123456"), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet()).harTilgang(avtale))
            .isFalse();
    }

    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        assertThat(new Arbeidsgiver(new Fnr("00000000001"), Set.of(), Map.of(), null, null).harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(this.bedriftNr, Set.of(Tiltakstype.values()));
        Arbeidsgiver Arbeidsgiver = new Arbeidsgiver(new Fnr("00000000009"), Set.of(), tilganger, null, null);
        assertThat(Arbeidsgiver.harTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avbrutt_avtale_eldre_enn_12_uker() {
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(this.bedriftNr, Set.of(Tiltakstype.values()));
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(new Fnr("00000000009"), Set.of(), tilganger, null, null);
        avtale.setAvbrutt(true);
        avtale.setSistEndret(Instant.now().minus(84, ChronoUnit.DAYS).minusMillis(100));
        assertThat(arbeidsgiver.harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setSluttDato(LocalDate.now().minusDays(85));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.values()));
        Arbeidsgiver Arbeidsgiver = new Arbeidsgiver(new Fnr("00000000009"), Set.of(), tilganger, null, null);
        assertThat(Arbeidsgiver.harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_ha_tilgang_til_avsluttet_avtale_eldre_enn_12_uker_når_ikke_godkjent_av_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setSluttDato(LocalDate.now().minusDays(85));
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.values()));
        Arbeidsgiver Arbeidsgiver = new Arbeidsgiver(new Fnr("00000000009"), Set.of(), tilganger, null, null);
        assertThat(Arbeidsgiver.harTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__arbeidsgiver_med_arbeidsgivertilgang_skal_ikke_ha_lonnstilskuddtilgang() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(avtale.getBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING));
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(new Fnr("00000000009"), Set.of(), tilganger, null, null);
        assertThat(arbeidsgiver.harTilgang(avtale)).isFalse();
    }
}
