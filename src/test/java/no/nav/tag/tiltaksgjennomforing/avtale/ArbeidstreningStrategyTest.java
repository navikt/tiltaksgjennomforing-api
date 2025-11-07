package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields.antallDagerPerUke;
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
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enArbeidstreningAvtaleMedAltUtfylt;
import static org.assertj.core.api.Assertions.assertThat;

class ArbeidstreningStrategyTest {

    private Avtale avtale;
    private AvtaleInnhold avtaleInnhold;
    private AvtaleInnholdStrategy strategy;

    @BeforeEach
    public void setUp() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        avtale = enArbeidstreningAvtaleMedAltUtfylt();
        avtaleInnhold = avtale.getGjeldendeInnhold();
        strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, Tiltakstype.ARBEIDSTRENING);
    }

    @Test
    void test_at_felter_relevante_i_arbeidstrening_kan_endres() {
        strategy.endre(TestData.endringPåAlleArbeidstreningFelter());

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
                arbeidsgiverTlf,
                veilederFornavn,
                veilederEtternavn,
                veilederTlf,
                stillingstittel,
                arbeidsoppgaver,
                stillingprosent,
                antallDagerPerUke,
                startDato,
                sluttDato,
                tilrettelegging,
                oppfolging,
                stillingKonseptId,
                stillingStyrk08
        ).filteredOn(Objects::isNull).isEmpty();
    }
}
