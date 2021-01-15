package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.arbeidsgiverEtternavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.arbeidsgiverFornavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.arbeidsgiverTlf;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.arbeidsoppgaver;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.bedriftNavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.deltakerEtternavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.deltakerFornavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.deltakerTlf;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.oppfolging;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.sluttDato;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.startDato;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.stillingKonseptId;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.stillingStyrk08;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.stillingprosent;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.stillingstittel;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.tilrettelegging;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.veilederEtternavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.veilederFornavn;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.veilederTlf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArbeidstreningStrategyTest {

    private AvtaleInnhold avtaleInnhold;
    private AvtaleInnholdStrategy strategy;

    @BeforeEach
    private void setUp() {
        avtaleInnhold = new AvtaleInnhold();
        strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.ARBEIDSTRENING);
    }

    @Test
    void test_at_felter_relevante_i_arbeidstrening_kan_endres() {
        strategy.endre(TestData.endringPåAlleFelter());

        // Test for collections
        assertThat(avtaleInnhold.getMaal()).isNotEmpty();

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
                stillingKonseptId,
                stillingStyrk08,
                arbeidsoppgaver
        ).filteredOn(Objects::isNull).isEmpty();
    }

    @Test
    void test_at_alt_er_utfylt() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        strategy.endre(endreAvtale);
        assertThat(strategy.erAltUtfylt()).isTrue();
    }
}