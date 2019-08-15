package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InnloggetBrukerTest {

    private Fnr deltaker;
    private Fnr fnrSomIkkeErDeltaker;
    private Avtale avtale;
    private BedriftNr bedriftNr;

    @Before
    public void setup() {
        deltaker = TestData.etFodselsnummerForDato(1990, 5, 5);
        fnrSomIkkeErDeltaker = TestData.etFodselsnummerForDato(1990, 6, 6);
        bedriftNr = new BedriftNr(TestData.GYLDIG_BEDRIFTSNR);
        avtale = Avtale.nyAvtale(new OpprettAvtale(deltaker, bedriftNr), new NavIdent("X100000"));
    }

    @Test
    public void deltakerKnyttetTilAvtaleSkalHaDeltakerRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBrukerUtenOrganisasjon(TestData.enDeltaker(avtale));
        assertThat(selvbetjeningBruker.avtalepart(avtale)).isInstanceOf(Deltaker.class);
    }

    @Test
    public void arbeidsgiverKnyttetTilAvtaleSkalHaArbeidsgiverRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBrukerMedOrganisasjon(TestData.enArbeidsgiver(avtale));
        assertThat(selvbetjeningBruker.avtalepart(avtale)).isInstanceOf(Arbeidsgiver.class);
    }

    @Test
    public void veilederKnyttetTilAvtaleSkalHaVeilederRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetNavAnsatt navAnsatt = TestData.innloggetNavAnsatt(TestData.enVeileder(avtale));
        assertThat(navAnsatt.avtalepart(avtale)).isInstanceOf(Veileder.class);
    }

    @Test
    public void harTilgang__deltaker_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetSelvbetjeningBruker(deltaker).harTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetNavAnsatt(TestData.enVeileder(avtale).getIdentifikator()).harTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetNavAnsatt(new NavIdent("X123456")).harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetSelvbetjeningBruker(fnrSomIkkeErDeltaker).harTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = new InnloggetSelvbetjeningBruker(fnrSomIkkeErDeltaker);
        innloggetSelvbetjeningBruker.getOrganisasjoner().add(new Organisasjon(bedriftNr, "Testbutikken"));
        assertThat(innloggetSelvbetjeningBruker.harTilgang(avtale)).isTrue();
    }
}