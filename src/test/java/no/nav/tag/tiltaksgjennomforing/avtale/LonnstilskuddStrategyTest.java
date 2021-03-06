package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.VARIG_LONNSTILSKUDD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMidlertidigLonnstilskuddException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LonnstilskuddStrategyTest {

    private AvtaleInnhold avtaleInnhold;
    private AvtaleInnholdStrategy strategy;

    @BeforeEach
    public void setUp() {
        avtaleInnhold = new AvtaleInnhold();
        strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, MIDLERTIDIG_LONNSTILSKUDD);
    }

    @Test
    void test_at_alt_er_ikke_utfylt_nar_stillingstype_er_null() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStillingstype(null);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isFalse();
    }

    @Test
    void test_at_feil_når_familietilknytning_ikke_er_fylt_ut() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring(null);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isFalse();
    }

    @Test
    void test_at_ikke_feil_når_ikke_forklaring_og_nei_på_familietilknytning() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setHarFamilietilknytning(false);
        endreAvtale.setFamilietilknytningForklaring(null);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_ikke_feil_når_alt_fylt_ut_og_har_familietilknytning() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("En god forklaring");
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_ingeting_er_utfylt_gir_false() {
        assertThat(strategy.erAltUtfylt()).isFalse();
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(24);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }


    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertThatThrownBy(() -> strategy.endre(endreAvtale)).isInstanceOf(VarighetForLangMidlertidigLonnstilskuddException.class);
    }

    @Test
    public void endreVarigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd() {
        strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, VARIG_LONNSTILSKUDD);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_styrk08_og_konsept_id_lagres_og_ikke_er_påkrevd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStillingKonseptId(null);
        endreAvtale.setStillingStyrk08(null);
        strategy.endre(endreAvtale);
        assertThat(avtaleInnhold.getStillingKonseptId()).isNull();
        assertThat(avtaleInnhold.getStillingStyrk08()).isNull();
        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_styrk08_og_konsept_id_lagres() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        strategy.endre(endreAvtale);
        assertThat(avtaleInnhold.getStillingKonseptId()).isNotNull();
        assertThat(avtaleInnhold.getStillingStyrk08()).isNotNull();
        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_alt_er_utfylt() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        strategy.endre(endreAvtale);
        assertThat(strategy.erAltUtfylt()).isTrue();
    }

}