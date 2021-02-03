package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltMåVæreFyltUtException;
import no.nav.tag.tiltaksgjennomforing.exceptions.AvtaleErIkkeFordeltException;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangArbeidstreningException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class AvtaleTest {

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("01234567890");

        NavIdent veilederNavIdent = new NavIdent("X123456");
        BedriftNr bedriftNr = new BedriftNr("000111222");
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isNotNull();
            softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
            softly.assertThat(avtale.getDeltakerTlf()).isNull();
            softly.assertThat(avtale.getMaal()).isEmpty();
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
            softly.assertThat(avtale.getSluttDato()).isNull();
            softly.assertThat(avtale.getStillingprosent()).isNull();
            softly.assertThat(avtale.getStillingstittel()).isNull();
            softly.assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
            softly.assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.erGodkjentAvVeileder()).isFalse();
        });
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        Avtale.veilederOppretterAvtale(new OpprettAvtale(null, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING), new NavIdent("X123456"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerArbeidsgiver() {
        Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("12345678901"), null, Tiltakstype.ARBEIDSTRENING), new NavIdent("X123456"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("11223344555"), new BedriftNr("000111222"), Tiltakstype.ARBEIDSTRENING), null);
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void sjekkVersjon__ugyldig_versjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.sjekkSistEndret(Instant.MIN);
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void sjekkVersjon__null() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.sjekkSistEndret(null);
    }

    @Test
    public void sjekkVersjon__gyldig_versjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.sjekkSistEndret(Instant.now());
    }

    @Test
    public void endreAvtaleSkalOppdatereRiktigeFelt() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = TestData.endringPåAlleArbeidstreningFelter();
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getDeltakerFornavn()).isEqualTo(endreAvtale.getDeltakerFornavn());
            softly.assertThat(avtale.getDeltakerEtternavn()).isEqualTo(endreAvtale.getDeltakerEtternavn());
            softly.assertThat(avtale.getDeltakerTlf()).isEqualTo(endreAvtale.getDeltakerTlf());
            softly.assertThat(avtale.getBedriftNavn()).isEqualTo(endreAvtale.getBedriftNavn());
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isEqualTo(endreAvtale.getArbeidsgiverFornavn());
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isEqualTo(endreAvtale.getArbeidsgiverEtternavn());
            softly.assertThat(avtale.getArbeidsgiverTlf()).isEqualTo(endreAvtale.getArbeidsgiverTlf());
            softly.assertThat(avtale.getVeilederFornavn()).isEqualTo(endreAvtale.getVeilederFornavn());
            softly.assertThat(avtale.getVeilederEtternavn()).isEqualTo(endreAvtale.getVeilederEtternavn());
            softly.assertThat(avtale.getVeilederTlf()).isEqualTo(endreAvtale.getVeilederTlf());
            softly.assertThat(avtale.getOppfolging()).isEqualTo(endreAvtale.getOppfolging());
            softly.assertThat(avtale.getTilrettelegging()).isEqualTo(endreAvtale.getTilrettelegging());
            softly.assertThat(avtale.getStartDato()).isEqualTo(endreAvtale.getStartDato());
            softly.assertThat(avtale.getSluttDato()).isEqualTo(endreAvtale.getSluttDato());
            softly.assertThat(avtale.getStillingprosent()).isEqualTo(endreAvtale.getStillingprosent());
            softly.assertThat(avtale.getMaal()).isEqualTo(endreAvtale.getMaal());
            softly.assertThat(avtale.getStillingstittel()).isEqualTo(endreAvtale.getStillingstittel());
        });
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void endreAvtale__for_langt_maal_skal_feile() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Maal etMaal = TestData.etMaal();
        etMaal.setBeskrivelse("Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.");
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setMaal(List.of(etMaal));
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
    }

    @Test
    public void endreAvtale__startdato_satt_men_ikke_sluttdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = LocalDate.now();
        endreAvtale.setStartDato(startDato);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getStartDato()).isEqualTo(startDato);
    }

    @Test
    public void endreAvtale__sluttdato_satt_men_ikke_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate sluttDato = LocalDate.now();
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getSluttDato()).isEqualTo(sluttDato);
    }

    @Test
    public void endreAvtale__startdato_og_sluttdato_satt_18mnd() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(18);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getStartDato()).isEqualTo(startDato);
        assertThat(avtale.getSluttDato()).isEqualTo(sluttDato);
    }

    @Test(expected = VarighetForLangArbeidstreningException.class)
    public void endreAvtale__startdato_og_sluttdato_satt_over_18mnd() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(18).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
    }

    @Test(expected = StartDatoErEtterSluttDatoException.class)
    public void endreAvtale__startdato_er_etter_sluttdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
    }

    @Test
    public void endreAvtaleSkalIkkeEndreGodkjenninger() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        boolean deltakerGodkjenningFoerEndring = avtale.erGodkjentAvDeltaker();
        boolean arbeidsgiverGodkjenningFoerEndring = avtale.erGodkjentAvArbeidsgiver();
        boolean veilederGodkjenningFoerEndring = avtale.erGodkjentAvVeileder();

        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deltakerGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvDeltaker());
            softly.assertThat(arbeidsgiverGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvArbeidsgiver());
            softly.assertThat(veilederGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvVeileder());
        });
    }

    @Test
    public void endreAvtaleSkalInkrementereVersjon() throws InterruptedException {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Instant førstEndret = avtale.getSistEndret();
        Thread.sleep(10);
        avtale.endreAvtale(førstEndret, TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(avtale.getSistEndret()).isAfter(førstEndret);
    }

    @Test(expected = AltMåVæreFyltUtException.class)
    public void kanIkkeGodkjennesNaarIkkeAltErUtfylt() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.sjekkOmAltErUtfylt();
    }

    @Test(expected = AltMåVæreFyltUtException.class)
    public void kanIkkeGodkjennesNaarDeltakerTlfMangler() {
        // Deltaker tlf ble innført etter at avtaler er opprettet. Det kan derfor være
        // avtaler som er godkjent av deltaker og AG som mangler tlf.
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setDeltakerTlf(null);
        avtale.sjekkOmAltErUtfylt();
    }

    @Test
    public void kanGodkjennesNaarAltErUtfylt() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.sjekkOmAltErUtfylt();
    }

    @Test
    public void status__ny_avtale() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        assertThat(avtale.status()).isEqualTo(Status.PÅBEGYNT.getStatusVerdi());
    }

    @Test
    public void status__null_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setStartDato(null);
        avtale.setSluttDato(null);
        assertThat(avtale.status()).isEqualTo(Status.PÅBEGYNT.getStatusVerdi());
    }

    @Test
    public void status__noe_fylt_ut() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setStartDato(LocalDate.now().plusDays(5));
        avtale.setSluttDato(avtale.getStartDato().plusMonths(3));
        avtale.setBedriftNavn("testbedriftsnavn");
        assertThat(avtale.status()).isEqualTo(Status.PÅBEGYNT.getStatusVerdi());
    }

    @Test
    public void status__avsluttet_i_gaar() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().minusWeeks(4).minusDays(1));
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo(Status.AVSLUTTET.getStatusVerdi());
    }

    @Test
    public void status__avslutter_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().minusWeeks(4));
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo(Status.GJENNOMFØRES.getStatusVerdi());
    }

    @Test
    public void status__startet_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now());
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo(Status.GJENNOMFØRES.getStatusVerdi());
    }

    @Test
    public void status__starter_i_morgen() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().plusDays(1));
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo(Status.KLAR_FOR_OPPSTART.getStatusVerdi());
    }

    @Test
    public void status__klar_for_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        assertThat(avtale.status()).isEqualTo(Status.MANGLER_GODKJENNING.getStatusVerdi());
    }

    @Test
    public void status__veileder_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().plusDays(1));
        avtale.setSluttDato(LocalDate.now().plusDays(1).plusMonths(1));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        assertThat(avtale.status()).isEqualTo(Status.KLAR_FOR_OPPSTART.getStatusVerdi());
    }

    @Test
    public void status__naar_deltaker_tlf_mangler() {
        // Deltaker tlf ble innført etter at avtaler er opprettet. Det kan derfor være
        // avtaler som er inngått som mangler tlf.
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().minusDays(1));
        avtale.setSluttDato(LocalDate.now().minusDays(1).plusMonths(1));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setDeltakerTlf(null);
        assertThat(avtale.status()).isEqualTo(Status.GJENNOMFØRES.getStatusVerdi());
    }

    @Test
    public void status__avbrutt() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.avbryt(TestData.enVeileder(avtale), new AvbruttInfo(LocalDate.now(), "grunnen"));
        assertThat(avtale.status()).isEqualTo(Status.AVBRUTT.getStatusVerdi());
        assertThat(avtale.getAvbruttDato()).isNotNull();
        assertThat(avtale.getAvbruttGrunn()).isEqualTo("grunnen");
    }

    @Test
    public void avbryt_ufordelt_avtale_skal_bli_fordelt() {
        Avtale avtale = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));
        avtale.avbryt(veileder, new AvbruttInfo(LocalDate.now(), "grunnen"));

        assertThat(avtale.status()).isEqualTo(Status.AVBRUTT.getStatusVerdi());
        assertThat(avtale.erUfordelt()).isFalse();
        assertThat(avtale.getVeilederNavIdent()).isEqualTo(veileder.getIdentifikator());
    }

    @Test
    public void tom_avtale_kan_avbrytes() {
        Avtale tomAvtale = TestData.enArbeidstreningAvtale();
        assertThat(tomAvtale.kanAvbrytes()).isTrue();
    }

    @Test
    public void fylt_avtale_kan_avbrytes() {
        Avtale fyltAvtale = TestData.enAvtaleMedAltUtfylt();
        assertThat(fyltAvtale.kanAvbrytes()).isTrue();
    }

    @Test
    public void godkjent_av_veileder_avtale_kan_avbrytes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        assertThat(godkjentAvtale.kanAvbrytes()).isTrue();
    }

    @Test
    public void allerede_avbrutt_avtale_kan_ikke_avbrytes() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setAvbrutt(true);
        assertThat(avtale.kanAvbrytes()).isFalse();
    }

    @Test
    public void status__gjenopprettet() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setAvbrutt(true);
        avtale.setAvbruttDato(LocalDate.now());
        avtale.setAvbruttGrunn("enGrunn");

        avtale.gjenopprett(TestData.enVeileder(avtale));
        assertThat(avtale.status()).isNotEqualTo(Status.AVBRUTT.getStatusVerdi());
        assertThat(avtale.getAvbruttDato()).isNull();
        assertThat(avtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void ikke_avbrutt_avtale_kan_ikke_gjenopprettes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        assertThat(godkjentAvtale.kanGjenopprettes()).isFalse();
    }

    @Test
    public void avbrutt_avtale_kan_gjenopprettes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        godkjentAvtale.setAvbrutt(true);
        assertThat(godkjentAvtale.kanGjenopprettes()).isTrue();
    }

    @Test
    public void kanLåsesOpp__skal_være_true_når_godkjent_av_veileder() {
        assertThat(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder().kanLåsesOpp()).isTrue();
    }

    @Test
    public void kanLåsesOpp__skal_være_false_når_ikke_godkjent_av_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setGodkjentAvVeileder(null);
        assertThat(avtale.kanLåsesOpp()).isFalse();
    }

    @Test
    public void låsOppAvtale__skal_ha_riktig_innhold() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Avtale avtaleKopi = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();

        avtale.låsOppAvtale();
        assertThat(avtale).isEqualToIgnoringGivenFields(avtaleKopi,
                Avtale.Fields.id,
                Avtale.Fields.opprettetTidspunkt,
                Avtale.Fields.sistEndret,
                Avtale.Fields.versjoner,
                AvtaleInnhold.Fields.godkjentAvDeltaker,
                AvtaleInnhold.Fields.godkjentAvArbeidsgiver,
                AvtaleInnhold.Fields.godkjentAvVeileder,
                "domainEvents");
        assertThat(avtale.getGodkjentAvDeltaker()).isNull();
        assertThat(avtale.getGodkjentAvArbeidsgiver()).isNull();
        assertThat(avtale.getGodkjentAvVeileder()).isNull();
        assertThat(avtale.getJournalpostId()).isNull();
    }

    @Test
    public void låsOppAvtale__skal_lage_ny_versjon() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.låsOppAvtale();
        List<AvtaleInnhold> versjoner = avtale.getVersjoner();

        AvtaleInnhold førsteVersjon = versjoner.get(0);
        AvtaleInnhold andreVersjon = versjoner.get(1);
        assertThat(førsteVersjon.getVersjon()).isEqualTo(1);
        assertThat(andreVersjon.getVersjon()).isEqualTo(2);
        assertThat(andreVersjon.getMaal())
                .usingElementComparatorOnFields(Maal.Fields.kategori, Maal.Fields.beskrivelse)
                .isEqualTo(førsteVersjon.getMaal());
        assertThat(andreVersjon).isEqualToIgnoringGivenFields(førsteVersjon,
                AvtaleInnhold.Fields.id,
                AvtaleInnhold.Fields.versjon,
                AvtaleInnhold.Fields.maal,
                AvtaleInnhold.Fields.godkjentAvDeltaker,
                AvtaleInnhold.Fields.godkjentAvArbeidsgiver,
                AvtaleInnhold.Fields.godkjentAvVeileder,
                AvtaleInnhold.Fields.journalpostId);
    }

    @Test
    public void sistEndretNå__kalles_ved_endreAvtale() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Thread.sleep(10);
        avtale.endreAvtale(Instant.MAX, TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForDeltaker() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForArbeidsgiver() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForVeileder() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForVeilederOgDeltaker() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Thread.sleep(10);
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_opphevGodkjenningerSomArbeidsgiver() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.opphevGodkjenningerSomArbeidsgiver();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_opphevGodkjenningerSomVeileder() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.opphevGodkjenningerSomVeileder();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_avbryt() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.avbryt(TestData.enVeileder(avtale), new AvbruttInfo());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_låsOppAvtale() throws InterruptedException {
        Instant førEndringen = Instant.now();
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Thread.sleep(10);
        avtale.låsOppAvtale();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void avtaleklarForOppstart() {
        Avtale avtale = TestData.enAvtaleKlarForOppstart();
        assertThat(avtale.status()).isEqualTo(Status.KLAR_FOR_OPPSTART.getStatusVerdi());
    }

    @Test
    public void avtale_opprettet_av_arbedsgiver_skal_være_ufordelt() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        assertThat(avtale.isOpprettetAvArbeidsgiver()).isTrue();
        assertThat(avtale.erUfordelt()).isTrue();
    }

    @Test
    public void avtale_kan_være_ufordelt_selv_om_alt_er_utfylt() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER);
        assertThat(avtale.erUfordelt()).isTrue();
    }

    @Test
    public void avtale_skal_kunne_godkjennes_når_den_erUfordelt() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThat(avtale.erGodkjentAvArbeidsgiver());
        assertThat(avtale.erGodkjentAvDeltaker());
    }

    @Test
    public void ufordelt_avtale_må_tildeles_før_veileder_godkjenner() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(
            new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThatThrownBy(() -> avtale.godkjennForVeileder(TestData.enNavIdent())).isInstanceOf(AvtaleErIkkeFordeltException.class);
    }


    @Test
    public void ufordelt_midlertidig_lts_avtale_endrer_avtale_med_lavere_lønnstilskuddprosent_enn_mellom_kun_40_60_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(20);
        assertThatThrownBy(() -> avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void ufordelt_midlertidig_lts_avtale_endrer_avtale_med_høyere_enn_maks_lønnstilskuddprosent_enn_60_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(67);
        assertThatThrownBy(() -> avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }


    @Test
    public void ufordelt_varig_lts_avtale_endrer_avtale_med_lavere_lønnstilskuddprosent_enn_0_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(-1);
        assertThatThrownBy(() -> avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void ufordelt_varig_lts_avtale_endrer_avtale_med_høyere_enn_maks_lønnstilskuddprosent_enn_75_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(100);
        assertThatThrownBy(() -> avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

}
