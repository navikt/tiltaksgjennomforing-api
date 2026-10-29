package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.ARBEIDSTRENING;
import static org.assertj.core.api.Assertions.assertThat;

class ArbeidstreningStartOgSluttDatoStrategyTest {

    @BeforeEach
    public void setup() {
        Now.fixedDate(LocalDate.of(2026, 10, 29));
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        Now.resetClock();
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_Arbeidstrening() {
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(Fnr.generer(1952,8,12), TestData.etBedriftNr(), ARBEIDSTRENING), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        ArbeidstreningStartOgSluttdatoStrategy arbeidstreningStartOgSluttDatoStrategy = new ArbeidstreningStartOgSluttdatoStrategy(avtale);
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> arbeidstreningStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt));
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = Innsatsgruppe.class, names = {
            "TRENGER_VEILEDNING", "TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE",
            "LITEN_MULIGHET_TIL_A_JOBBE", "JOBBE_DELVIS"
    })
    void kan_forkorte_for_lang_inngatt_avtale_til_fortiden(Innsatsgruppe innsatsgruppe) {
        Avtale avtale = en_inngatt_avtale(innsatsgruppe);
        LocalDate nySluttDato = Now.localDate().minusDays(1);

        avtale.forkortAvtale(nySluttDato, ForkortetGrunn.av(ForkortetGrunn.FULLFØRT, null), TestData.enNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(nySluttDato);
        assertThat(avtale.getStatus()).isEqualTo(Status.AVSLUTTET);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "NULL, VARIGHET_FOR_LANG_ARBEIDSTRENING_12_MND",
            "TRENGER_VEILEDNING, VARIGHET_FOR_LANG_ARBEIDSTRENING_12_MND",
            "JOBBE_DELVIS, VARIGHET_FOR_LANG_ARBEIDSTRENING_18_MND"
    }, nullValues = "NULL")
    void kan_ikke_forlenge_utover_varighetsgrensen_selv_om_ny_sluttdato_er_i_fortiden(
            Innsatsgruppe innsatsgruppe, Feilkode feilkode) {
        Avtale avtale = en_inngatt_avtale(innsatsgruppe);
        LocalDate gammelSluttDato = Now.localDate().minusDays(2);
        avtale.getGjeldendeInnhold().setSluttDato(gammelSluttDato);

        assertFeilkode(feilkode, () -> avtale.forlengAvtale(Now.localDate().minusDays(1), TestData.enNavIdent()));

        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(gammelSluttDato);
    }

    @ParameterizedTest
    @CsvSource({
            "TRENGER_VEILEDNING, 0, VARIGHET_FOR_LANG_ARBEIDSTRENING_12_MND",
            "TRENGER_VEILEDNING, 1, VARIGHET_FOR_LANG_ARBEIDSTRENING_12_MND",
            "JOBBE_DELVIS, 0, VARIGHET_FOR_LANG_ARBEIDSTRENING_18_MND",
            "JOBBE_DELVIS, 1, VARIGHET_FOR_LANG_ARBEIDSTRENING_18_MND"
    })
    void kan_ikke_forkorte_utover_varighetsgrensen_til_i_dag_eller_fremtiden(
            Innsatsgruppe innsatsgruppe, int dagerFraIDag, Feilkode feilkode) {
        Avtale avtale = en_inngatt_avtale(innsatsgruppe);

        assertFeilkode(feilkode, () -> avtale.forkortAvtale(
                Now.localDate().plusDays(dagerFraIDag),
                ForkortetGrunn.av(ForkortetGrunn.FULLFØRT, null), TestData.enNavIdent()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "NULL, VARIGHET_FOR_LANG_ARBEIDSTRENING_12_MND",
            "TRENGER_VEILEDNING, VARIGHET_FOR_LANG_ARBEIDSTRENING_12_MND",
            "TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, VARIGHET_FOR_LANG_ARBEIDSTRENING_18_MND",
            "LITEN_MULIGHET_TIL_A_JOBBE, VARIGHET_FOR_LANG_ARBEIDSTRENING_18_MND",
            "JOBBE_DELVIS, VARIGHET_FOR_LANG_ARBEIDSTRENING_18_MND"
    }, nullValues = "NULL")
    void forkortingsunntaket_gjelder_ikke_avtaler_som_ikke_er_inngatt(
            Innsatsgruppe innsatsgruppe, Feilkode feilkode) {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setInnsatsgruppe(innsatsgruppe);
        avtale.setGodkjentForEtterregistrering(true);
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusYears(2));

        assertFeilkode(feilkode, () -> new ArbeidstreningStartOgSluttdatoStrategy(avtale)
                .sjekkSluttdatoVedForkorting(Now.localDate().minusDays(1)));
    }

    @Test
    void forkortingsunntaket_tillater_ikke_sluttdato_for_startdato() {
        Avtale avtale = en_inngatt_avtale(Innsatsgruppe.TRENGER_VEILEDNING);

        assertFeilkode(Feilkode.START_ETTER_SLUTT, () -> avtale.forkortAvtale(
                avtale.getGjeldendeInnhold().getStartDato().minusDays(1),
                ForkortetGrunn.av(ForkortetGrunn.FULLFØRT, null), TestData.enNavIdent()));
    }

    @Test
    void forkortingsunntaket_opphever_ikke_aldersgrensen() {
        Avtale avtale = en_inngatt_avtale(Innsatsgruppe.TRENGER_VEILEDNING);
        avtale.setDeltakerFnr(Fnr.generer(1952, 8, 12));

        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> avtale.forkortAvtale(
                Now.localDate().minusDays(1),
                ForkortetGrunn.av(ForkortetGrunn.FULLFØRT, null), TestData.enNavIdent()));
    }

    private Avtale en_inngatt_avtale(Innsatsgruppe innsatsgruppe) {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setInnsatsgruppe(innsatsgruppe);
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusYears(2));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusDays(2));
        return avtale;
    }
}
