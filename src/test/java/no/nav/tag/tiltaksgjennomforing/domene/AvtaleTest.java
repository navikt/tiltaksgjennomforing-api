package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleTest {

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("01234567890");

        NavIdent veilederNavIdent = new NavIdent("X123456");
        BedriftNr bedriftNr = new BedriftNr("000111222");
        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(deltakerFnr, bedriftNr), veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isNull();
            softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
            softly.assertThat(avtale.getDeltakerTlf()).isNull();
            softly.assertThat(avtale.getMaal()).isEmpty();
            softly.assertThat(avtale.getOppgaver()).isEmpty();
            softly.assertThat(avtale.getDeltakerFornavn()).isNull();
            softly.assertThat(avtale.getDeltakerEtternavn()).isNull();
            softly.assertThat(avtale.getBedriftNavn()).isNull();
            softly.assertThat(avtale.getBedriftNr()).isEqualTo(bedriftNr);
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverTlf()).isNull();
            softly.assertThat(avtale.getVeilederFornavn()).isNull();
            softly.assertThat(avtale.getVeilederEtternavn()).isNull();
            softly.assertThat(avtale.getVeilederTlf()).isNull();
            softly.assertThat(avtale.getOppfolging()).isNull();
            softly.assertThat(avtale.getTilrettelegging()).isNull();
            softly.assertThat(avtale.getStartDato()).isNull();
            softly.assertThat(avtale.getArbeidstreningLengde()).isNull();
            softly.assertThat(avtale.getArbeidstreningStillingprosent()).isNull();
            softly.assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
            softly.assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.erGodkjentAvVeileder()).isFalse();
        });
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        Avtale.nyAvtale(new OpprettAvtale(null, new BedriftNr("111222333")), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerArbeidsgiver() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("12345678901"), null), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("11223344555"), new BedriftNr("000111222")), null);
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void sjekkVersjon__ugyldig_versjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(-1);
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void sjekkVersjon__null() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(null);
    }

    @Test
    public void sjekkVersjon__gyldig_versjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(avtale.getVersjon());
    }

    @Test
    public void endreAvtaleSkalOppdatereRiktigeFelt() {
        Avtale avtale = TestData.enAvtale();
        EndreAvtale endreAvtale = TestData.endringPaAlleFelt();
        avtale.endreAvtale(avtale.getVersjon(), endreAvtale, Avtalerolle.VEILEDER);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getDeltakerFornavn()).isEqualTo(endreAvtale.getDeltakerFornavn());
            softly.assertThat(avtale.getDeltakerEtternavn()).isEqualTo(endreAvtale.getDeltakerEtternavn());
            softly.assertThat(avtale.getDeltakerTlf()).isEqualTo(endreAvtale.getDeltakerTlf());
            softly.assertThat(avtale.getBedriftNavn()).isEqualTo(endreAvtale.getBedriftNavn());
            softly.assertThat(avtale.getBedriftNr()).isEqualTo(endreAvtale.getBedriftNr());
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isEqualTo(endreAvtale.getArbeidsgiverFornavn());
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isEqualTo(endreAvtale.getArbeidsgiverEtternavn());
            softly.assertThat(avtale.getArbeidsgiverTlf()).isEqualTo(endreAvtale.getArbeidsgiverTlf());
            softly.assertThat(avtale.getVeilederFornavn()).isEqualTo(endreAvtale.getVeilederFornavn());
            softly.assertThat(avtale.getVeilederEtternavn()).isEqualTo(endreAvtale.getVeilederEtternavn());
            softly.assertThat(avtale.getVeilederTlf()).isEqualTo(endreAvtale.getVeilederTlf());
            softly.assertThat(avtale.getOppfolging()).isEqualTo(endreAvtale.getOppfolging());
            softly.assertThat(avtale.getTilrettelegging()).isEqualTo(endreAvtale.getTilrettelegging());
            softly.assertThat(avtale.getStartDato()).isEqualTo(endreAvtale.getStartDato());
            softly.assertThat(avtale.getArbeidstreningLengde()).isEqualTo(endreAvtale.getArbeidstreningLengde());
            softly.assertThat(avtale.getArbeidstreningStillingprosent()).isEqualTo(endreAvtale.getArbeidstreningStillingprosent());
            softly.assertThat(avtale.getMaal()).isEqualTo(endreAvtale.getMaal());
            softly.assertThat(avtale.getOppgaver()).isEqualTo(endreAvtale.getOppgaver());
        });
    }

    @Test
    public void endreAvtaleSkalIkkeEndreGodkjenninger() {
        Avtale avtale = TestData.enAvtale();
        boolean deltakerGodkjenningFoerEndring = avtale.erGodkjentAvDeltaker();
        boolean arbeidsgiverGodkjenningFoerEndring = avtale.erGodkjentAvArbeidsgiver();
        boolean veilederGodkjenningFoerEndring = avtale.erGodkjentAvVeileder();

        avtale.endreAvtale(avtale.getVersjon(), TestData.endringPaAlleFelt(), Avtalerolle.VEILEDER);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deltakerGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvDeltaker());
            softly.assertThat(arbeidsgiverGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvArbeidsgiver());
            softly.assertThat(veilederGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvVeileder());
        });
    }

    @Test
    public void endreAvtaleSkalInkrementereVersjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(avtale.getVersjon()).isEqualTo(2);
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void kanIkkeGodkjennesNaarIkkeAltErUtfylt() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkOmKanGodkjennes();
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void kanIkkeGodkjennesNaarDeltakerTlfMangler() {
        // Deltaker tlf ble innført etter at avtaler er opprettet. Det kan derfor være
        // avtaler som er godkjent av deltaker og AG som mangler tlf.
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setDeltakerTlf(null);
        avtale.sjekkOmKanGodkjennes();
    }

    @Test
    public void kanGodkjennesNaarAltErUtfylt() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.sjekkOmKanGodkjennes();
    }

    @Test
    public void status__ny_avtale() {
        Avtale avtale = TestData.enAvtale();
        assertThat(avtale.status()).isEqualTo("Påbegynt");
    }

    @Test
    public void status__null_startdato() {
        Avtale avtale = TestData.enAvtale();
        avtale.setStartDato(null);
        avtale.setArbeidstreningLengde(null);
        assertThat(avtale.status()).isEqualTo("Påbegynt");
    }

    @Test
    public void status__noe_fylt_ut() {
        Avtale avtale = TestData.enAvtale();
        avtale.setStartDato(LocalDate.now().plusDays(5));
        avtale.setArbeidstreningLengde(12);
        avtale.setBedriftNavn("testbedriftsnavn");
        assertThat(avtale.status()).isEqualTo("Påbegynt");
    }

    @Test
    public void status__avsluttet_i_gaar() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().minusWeeks(4).minusDays(1));
        avtale.setArbeidstreningLengde(4);
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo("Avsluttet");
    }

    @Test
    public void status__avslutter_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().minusWeeks(4));
        avtale.setArbeidstreningLengde(4);
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo("Klar for oppstart");
    }

    @Test
    public void status__klar_for_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        assertThat(avtale.status()).isEqualTo("Mangler godkjenning");
    }

    @Test
    public void status__veileder_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo("Klar for oppstart");
    }

    @Test
    public void status__naar_deltaker_tlf_mangler() {
        // Deltaker tlf ble innført etter at avtaler er opprettet. Det kan derfor være
        // avtaler som er inngått som mangler tlf.
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setDeltakerTlf(null);
        assertThat(avtale.status()).isEqualTo("Klar for oppstart");
    }

    @Test
    public void tom_avtale_kan_avbrytes() {
        Avtale tomAvtale = TestData.enAvtale();
        assertThat(tomAvtale.kanAvbrytes());
    }

    @Test
    public void fylt_avtale_kan_avbrytes() {
        Avtale fyltAvtale = TestData.enAvtaleMedAltUtfylt();
        assertThat(fyltAvtale.kanAvbrytes());
    }

    @Test
    public void godkjent_av_veileder_avtale_kan_ikke_avbrytes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        assertThat(!godkjentAvtale.kanAvbrytes());
    }
}
