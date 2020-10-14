package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.jupiter.api.Test;

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
}