package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.*;
import static org.assertj.core.api.Assertions.assertThat;

class ArbeidstreningStrategyTest {
    @Test
    void test_at_felter_relevante_i_arbeidstrening_kan_endres() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.ARBEIDSTRENING);

        strategy.endre(TestData.endringPåAlleFelter());

        // Test for collections
        assertThat(avtaleInnhold.getMaal()).isNotEmpty();
        assertThat(avtaleInnhold.getOppgaver()).isNotEmpty();

        // Test for ikke-collections
        assertThat(avtaleInnhold).extracting(
                deltakerFornavn,
                deltakerEtternavn,
                deltakerTlf,
                bedriftNavn,
                arbeidsgiverFornavn,
                arbeidsgiverEtternavn,
                deltakerEtternavn,
                arbeidsgiverTlf,
                veilederFornavn,
                veilederEtternavn,
                veilederTlf,
                oppfolging,
                tilrettelegging,
                startDato,
                sluttDato,
                stillingprosent,
                stillingstittel,
                arbeidsoppgaver
        ).filteredOn(Objects::isNull).isEmpty();
    }

    // Test at arbeidstreningsavtale regnes som utfylt med arbeidsoppgaver registrert som kort (gammel løsning)
    @Test
    void test_at_alt_er_utfylt_uten_arbeidsoppgaver() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.ARBEIDSTRENING);

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setArbeidsoppgaver("");

        strategy.endre(endreAvtale);
        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    // "Test at arbeidstreningsavtale regnes som utfylt med arbeidsoppgaver registrert som fritekst (ny løsning)
    @Test
    void test_at_alt_er_utfylt_uten_oppgaver() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.ARBEIDSTRENING);

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setOppgaver(List.of());

        strategy.endre(endreAvtale);
        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    void test_at_ikke_alt_er_utfylt_uten_oppgaver_eller_arbeidsoppgaver() {
        AvtaleInnhold avtaleInnhold = new AvtaleInnhold();
        AvtaleInnholdStrategy strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.ARBEIDSTRENING);

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setOppgaver(List.of());
        endreAvtale.setArbeidsoppgaver("");

        strategy.endre(endreAvtale);
        assertThat(strategy.erAltUtfylt()).isFalse();
    }
}