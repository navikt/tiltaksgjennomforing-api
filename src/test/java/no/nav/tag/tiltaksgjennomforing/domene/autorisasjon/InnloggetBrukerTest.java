package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class InnloggetBrukerTest {

    private Fnr deltaker;
    private Fnr arbeidsgiver;
    private NavIdent navIdent;
    private Avtale avtale;
    private BedriftNr bedriftNr;

    @Before
    public void setup() {
        deltaker = new Fnr("00000000000");
        arbeidsgiver = new Fnr("10000000000");
        navIdent = new NavIdent("X100000");
        avtale = Avtale.nyAvtale(new OpprettAvtale(deltaker, arbeidsgiver, "Testbutikken"), navIdent);
        bedriftNr = new BedriftNr("12345678901");
        avtale.setBedriftNr(bedriftNr);
    }

    @Test
    public void harTilgang__deltaker_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetSelvbetjeningBruker(deltaker).harTilgang(avtale)).isTrue();
    }
    @Test
    public void harTilgang__veileder_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetNavAnsatt(navIdent).harTilgang(avtale)).isTrue();
    }
    @Test
    public void harTilgang__arbeidsgiver_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetSelvbetjeningBruker(arbeidsgiver).harTilgang(avtale)).isTrue();
    }
    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetNavAnsatt(new NavIdent("X123456")).harTilgang(avtale)).isFalse();
    }
    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetSelvbetjeningBruker(new Fnr("00000000001")).harTilgang(avtale)).isFalse();
    }
    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = new InnloggetSelvbetjeningBruker(new Fnr("00000000009"));
        innloggetSelvbetjeningBruker.getOrganisasjoner().add(new Organisasjon(bedriftNr, "Testbutikken"));
        assertThat(innloggetSelvbetjeningBruker.harTilgang(avtale)).isTrue();
    }
}