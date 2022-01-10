package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

public class RegnUtTilskuddsperioderForAvtaleTest {

    @Test
    public void en_tilskuddsperiode() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate fra = LocalDate.of(2021, 1, 1);
        LocalDate til = LocalDate.of(2021, 3, 31);

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(fra);
        endreAvtale.setSluttDato(til);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));


        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode().first().getBeløp()).isEqualTo(avtale.getSumLonnstilskudd() * 3);
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void splitt_ved_nyttår() {
        Now.fixedDate(LocalDate.of(2020, 12, 1));
        LocalDate fra = LocalDate.of(2020, 12, 1);
        LocalDate til = LocalDate.of(2021, 1, 31);

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(fra);
        endreAvtale.setSluttDato(til);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        var tilskuddPerioder = avtale.getTilskuddPeriode();

        assertThat(tilskuddPerioder.size()).isEqualTo(2);
        Iterator<TilskuddPeriode> iterator = tilskuddPerioder.iterator();
        TilskuddPeriode første = iterator.next();
        TilskuddPeriode andre = iterator.next();
        assertThat(første.getBeløp()).isEqualTo(andre.getBeløp());
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void reduksjon_etter_6_mnd__30_prosent_lonnstilskudd() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(40);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        TilskuddPeriode tilskuddpeirode6mndEtterStart = finnTilskuddsperiodeForDato(avtale.getStartDato().plusMonths(6), avtale);
        TilskuddPeriode tilskuddperiodeDagenFør6Mnd = finnTilskuddsperiodeForDato(avtale.getStartDato().plusMonths(6).minusDays(1), avtale);

        assertThat(tilskuddpeirode6mndEtterStart.getLonnstilskuddProsent()).isEqualTo(30);
        assertThat(tilskuddperiodeDagenFør6Mnd.getLonnstilskuddProsent()).isEqualTo(40);

        harRiktigeEgenskaper(avtale);
    }

    private TilskuddPeriode finnTilskuddsperiodeForDato(LocalDate dato, Avtale avtale) {
        for (TilskuddPeriode tilskuddsperiode : avtale.getTilskuddPeriode()) {
            if (tilskuddsperiode.getStartDato().isBefore(dato.plusDays(1)) && tilskuddsperiode.getSluttDato().isAfter(dato.minusDays(1))) {
                return tilskuddsperiode;
            }
        }
        return null;
    }

    @Test
    public void finnTilskuddsperiodeForDato() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 1, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 10, 1));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        TilskuddPeriode tilskuddPeriode1 = finnTilskuddsperiodeForDato(LocalDate.of(2021, 1, 1), avtale);
        TilskuddPeriode tilskuddPeriode2 = finnTilskuddsperiodeForDato(LocalDate.of(2021, 4, 1), avtale);

        assertThat(tilskuddPeriode1).isEqualTo(avtale.tilskuddsperiode(0));
        assertThat(tilskuddPeriode2).isEqualTo(avtale.tilskuddsperiode(1));
        Now.resetClock();

    }

    @Test
    public void sjekkAtEnhetsnrOgEnhetsnavnBlirSattPaEndreAvtale() {
        final String ENHETS_NR = "1001";
        final String ENHETS_NAVN = "NAV Ullensaker";

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(avtale.getStartDato());
        endreAvtale.setSluttDato(avtale.getSluttDato());

        avtale.oppdatereKostnadsstedForTilskuddsperioder(new NyttKostnadssted(ENHETS_NR, ENHETS_NAVN));
        assertThat(avtale.tilskuddsperiode(0).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(0).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);
        assertThat(avtale.tilskuddsperiode(1).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(2).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);

        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.tilskuddsperiode(0).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(0).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);
        assertThat(avtale.tilskuddsperiode(1).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(2).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);
    }

    @Test
    public void reduksjon_etter_12_mnd_60_prosent_lonnstilskudd() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(13));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        TilskuddPeriode sisteTilskuddsperiode = avtale.tilskuddsperiode(avtale.getTilskuddPeriode().size() - 1);
        assertThat(sisteTilskuddsperiode.getLonnstilskuddProsent()).isEqualTo(50);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void splitt_etter_reduksjon_30_prosnt_lonnstilskudd() {
        Now.fixedDate(LocalDate.of(2020, 6, 28));
        LocalDate startDato = LocalDate.of(2020, 6, 30);
        LocalDate sluttDato = LocalDate.of(2021, 1, 2);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(40);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);

        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(4);
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_reduksjon_skjer_etter_6_mnd_ved_40_prosent() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2021, 7, 1);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale.setLonnstilskuddProsent(40);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2021, 7, 1));
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_reduksjon_skjer_etter_12_mnd_ved_60_prosent() {
        Now.fixedDate(LocalDate.of(2021,1,1));
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2022, 1, 1);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale.setLonnstilskuddProsent(60);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2022, 1, 1));
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_ingen_redusering_under_1_år_60_prosent() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2021, 12, 31);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale.setLonnstilskuddProsent(60);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        assertThat(avtale.getDatoForRedusertProsent()).isNull();
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_varig_lonnstilskudd_ikke_reduserses() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 1, 1));
        endreAvtale.setSluttDato(LocalDate.of(2031, 1, 1));
        endreAvtale.setLonnstilskuddProsent(60);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        assertThat(avtale.tilskuddsperiode(avtale.getTilskuddPeriode().size() - 1).getLonnstilskuddProsent()).isEqualTo(60);
        assertThat(avtale.getDatoForRedusertProsent()).isNull();
        Now.resetClock();
    }

    @Test
    public void sjekk_at_avtalen_annulleres_ikke_om_den_har_en_utbetalt_tilskuddsperiode() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.UTBETALT);
        assertThrows(FeilkodeException.class, () ->   avtale.annuller(TestData.enVeileder(avtale), ""));
    }

    @Test
    public void sjekk_at_utbetalte_perioder_beholdes_ved_endring() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.UTBETALT);
        UUID idPåUtbetaltTilskuddsperiode = avtale.tilskuddsperiode(0).getId();
        Integer beløpPåUtbetaltTilskuddsperiode = avtale.tilskuddsperiode(0).getBeløp();

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåUtbetaltTilskuddsperiode);
        assertThat(avtale.tilskuddsperiode(0).getBeløp()).isEqualTo(beløpPåUtbetaltTilskuddsperiode);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void sjekk_at_nye_perioder_ved_forlengelse_starter_etter_utbetalte_perioder() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        Avtale avtale = TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 4, 1));


        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.UTBETALT);
        avtale.tilskuddsperiode(1).setStatus(TilskuddPeriodeStatus.UTBETALT);

        avtale.forlengAvtale(avtale.getSluttDato().plusMonths(3), TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);

        assertThat(avtale.tilskuddsperiode(1).getStartDato()).isEqualTo(avtale.tilskuddsperiode(0).getSluttDato().plusDays(1));
        assertThat(avtale.tilskuddsperiode(2).getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_godkjent_perioder_beholdes_ved_endring_som_ikke_påvirker_økonomi() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.tilskuddsperiode(0).getId();

        avtale.forlengAvtale(avtale.getSluttDato().plusDays(1), TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void sjekk_at_godkjent_perioder_beholdes_ved_endring_som_påvirker_økonomi() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        Integer beløpFørEndring = avtale.tilskuddsperiode(0).getBeløp();

        EndreTilskuddsberegning endreTilskuddsberegning = EndreTilskuddsberegning.builder()
                .manedslonn(99999)
                .arbeidsgiveravgift(avtale.getArbeidsgiveravgift())
                .feriepengesats(avtale.getFeriepengesats())
                .otpSats(avtale.getOtpSats())
                .build();
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(0).getBeløp()).isEqualTo(beløpFørEndring);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void sjekk_at_godkjent_periode_annulleres() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.tilskuddsperiode(0).getId();

        avtale.annuller(TestData.enVeileder(avtale), "");

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
    }

    @Test
    public void sjekk_at_ubehandlet_periode_slettes() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.UBEHANDLET);

        avtale.annuller(TestData.enVeileder(avtale), "");

        assertThat(avtale.getTilskuddPeriode()).isEmpty();
    }

    @Test
    public void sjekk_at_godkjent_periode_ikke_annulleres_ved_forlengelse() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate avtaleFørsteDag = LocalDate.of(2021, 1, 1);
        Avtale avtale = TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleFørsteDag, avtaleFørsteDag);

        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.tilskuddsperiode(0).getId();

        avtale.forlengAvtale(avtaleFørsteDag.plusDays(1), TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
        assertThat(avtale.tilskuddsperiode(0).getStartDato()).isEqualTo(avtaleFørsteDag);
        assertThat(avtale.tilskuddsperiode(0).getSluttDato()).isEqualTo(avtaleFørsteDag);

        assertThat(avtale.tilskuddsperiode(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
        assertThat(avtale.tilskuddsperiode(1).getStartDato()).isEqualTo(avtaleFørsteDag.plusDays(1));
        assertThat(avtale.tilskuddsperiode(1).getSluttDato()).isEqualTo(avtaleFørsteDag.plusDays(1));

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_godkjent_periode_ikke_annulleres_ved_økonomiendring_i_et_hull() {
        Now.fixedDate(LocalDate.of(2020, 6, 28));
        LocalDate avtaleStart = LocalDate.of(2021, 1, 1);
        LocalDate avtaleSlutt = LocalDate.of(2021, 8, 1);
        Avtale avtale = TestData.enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);


        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.UTBETALT);
        avtale.tilskuddsperiode(1).setStatus(TilskuddPeriodeStatus.GODKJENT);
        avtale.tilskuddsperiode(2).setStatus(TilskuddPeriodeStatus.UTBETALT);

        EndreTilskuddsberegning endreTilskuddsberegning = EndreTilskuddsberegning.builder()
                .manedslonn(77777)
                .arbeidsgiveravgift(avtale.getArbeidsgiveravgift())
                .feriepengesats(avtale.getFeriepengesats())
                .otpSats(avtale.getOtpSats())
                .build();
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(2).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    /* ------------ Metoder som kun brukes innad i denne test-klassen ------------ */
    private void harRiktigeEgenskaper(Avtale avtale) {
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
        harRiktigeLøpenumre(avtale.getTilskuddPeriode());
    }

    private void harAlleDageneIAvtalenperioden(Collection<TilskuddPeriode> tilskuddPerioder, LocalDate avtaleStart, LocalDate avtaleSlutt) {
        long antallDager = avtaleStart.until(avtaleSlutt.plusDays(1), ChronoUnit.DAYS);
        Set<LocalDate> dateSet = new HashSet<>();
        List<TilskuddPeriode> tilskuddsperioderUtenAnnullerte = tilskuddPerioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList());
        for (TilskuddPeriode periode : tilskuddsperioderUtenAnnullerte) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());
            dateSet.addAll(localDates);
        }
        if (dateSet.size() != antallDager) {
            fail("Ulikt antall dager i avtalen og tilskuddsperiodene");
        }

    }

    private void harOverlappendeDatoer(Collection<TilskuddPeriode> tilskuddPerioder) {
        Set<LocalDate> dateSet = new HashSet<>();
        List<TilskuddPeriode> tilskuddsperioderUtenAnnullerte = tilskuddPerioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList());
        for (TilskuddPeriode periode : tilskuddsperioderUtenAnnullerte) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());

            if (!Collections.disjoint(dateSet, localDates)) {
                fail("Det finnes overlappende datoer");
            } else {
                dateSet.addAll(localDates);
            }
        }
    }

    private void harRiktigeLøpenumre(Collection<TilskuddPeriode> tilskuddPerioder) {
        int løpenummer = 1;
        for (TilskuddPeriode tilskuddPeriode : tilskuddPerioder.stream().filter(tp -> tp.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList())) {
            assertThat(tilskuddPeriode.getLøpenummer()).isEqualTo(løpenummer++);
        }
    }


    /* ------------ Tester av metoder som kun brukes innad i denne test-klassen ------------ */

    @Test
    public void sjekk_at_ikkeoverlappende_periode_ikke_har_overlappende_datoer() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 1), 60);
        harOverlappendeDatoer(new TreeSet<>(Set.of(tilskuddPeriode1, tilskuddPeriode2)));
    }

    @Test
    public void sjekk_at_har_overlappende_datoer() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 3, 31), LocalDate.of(2021, 6, 1), 60);
        assertThatThrownBy(() -> harOverlappendeDatoer(List.of(tilskuddPeriode1, tilskuddPeriode2))).isInstanceOf(AssertionError.class);
    }

    @Test
    public void sjekk_at_alle_dagene_i_avtalen_er_i_tilskuddsperiodene() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 1), 60);
        harAlleDageneIAvtalenperioden(List.of(tilskuddPeriode1, tilskuddPeriode2), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 6, 1));
    }

    @Test
    public void sjekk_at_det_feiler_nar_ikke_alle_dagene_i_avtalen_er_i_tilskuddsperiodene() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 25), 60);
        assertThatThrownBy(() -> harAlleDageneIAvtalenperioden(List.of(tilskuddPeriode1, tilskuddPeriode2), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 26))).isInstanceOf(AssertionError.class);
    }
}