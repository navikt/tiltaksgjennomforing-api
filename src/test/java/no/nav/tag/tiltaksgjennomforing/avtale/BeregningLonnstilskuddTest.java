package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BeregningLonnstilskuddTest {

    private AvtaleInnhold avtaleInnhold;
    private AvtaleInnholdStrategy strategy;

    @BeforeEach
    public void setUp() {
        avtaleInnhold = new AvtaleInnhold();
        strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, MIDLERTIDIG_LONNSTILSKUDD);
    }

    @Test
    public void test_regn_ut_sumLonntilskudd_til_0_nar_lonnstilskudd_prosent_er_null() {
        // GIVEN
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setManedslonn(20000);
        endreAvtale.setFeriepengesats(new BigDecimal(12));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal(14.1));

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getSumLonnstilskudd()).isEqualTo(0);
    }

    @Test
    public void test_regn_ut_sumLonntilskudd() {
        // GIVEN
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setManedslonn(20000);
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setFeriepengesats(new BigDecimal(12));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal(14.1));

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getSumLonnstilskudd()).isEqualTo(15642);
    }

    @Test
    public void test_regn_ut_arbeidsgiveravgiftbelop() {
        // GIVEN
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setManedslonn(20000);
        endreAvtale.setFeriepengesats(new BigDecimal(12));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal(14.1));

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getArbeidsgiveravgiftBelop()).isEqualTo(3222);
    }

    @Test
    public void test_regn_ut_oblig_tjenstepensjon() {
        // GIVEN
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setManedslonn(20000);
        endreAvtale.setFeriepengesats(new BigDecimal(12));

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getOtpBelop()).isEqualTo(448);
    }

    @Test
    public void test_regn_ut_oblig_tjenstepensjon_til_null_om_Feriepengersats_er_null() {
        // GIVEN
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setFeriepengesats(null);

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getOtpBelop()).isNull();
        assertThat(avtaleInnhold.getFeriepengerBelop()).isNull();
    }

    @Test
    public void test_regn_ut_oblig_tjenstepensjon_til_null_om_manedslonn_er_null() {
        // GIVEN
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setManedslonn(null);

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getOtpBelop()).isNull();
        assertThat(avtaleInnhold.getFeriepengerBelop()).isNull();
    }

    @Test
    public void test_regn_ut_feriepenger_belop() {
        // GIVEN
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setManedslonn(20000);
        endreAvtale.setFeriepengesats(new BigDecimal(12));

        // WHEN
        strategy.endre(endreAvtale);

        // THEN
        assertThat(avtaleInnhold.getFeriepengerBelop()).isEqualTo(2400);
    }


}
