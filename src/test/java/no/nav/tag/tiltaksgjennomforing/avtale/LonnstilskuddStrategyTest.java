package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LonnstilskuddStrategyTest {

    @Test
    void test_at_feil_når_familietilknytning_ikke_er_fylt_ut() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring(null);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isFalse();
    }

    @Test
    void test_at_ikke_feil_når_ikke_forklaring_og_nei_på_familietilknytning() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setHarFamilietilknytning(false);
        endreAvtale.setFamilietilknytningForklaring(null);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_ikke_feil_når_alt_fylt_ut_og_har_familietilknytning() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("En god forklaring");
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_ingeting_er_utfylt_gir_false() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        assertThat(strategy.erAltUtfylt()).isFalse();
    }

}