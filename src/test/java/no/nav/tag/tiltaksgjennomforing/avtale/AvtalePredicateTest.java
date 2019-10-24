package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtalePredicateTest {
    @Test
    void veileder_nav_ident_oppgitt() {
        Avtale avtale = TestData.enAvtale();
        AvtalePredicate query = new AvtalePredicate();
        query.setVeilederNavIdent(avtale.getVeilederNavIdent());
        assertThat(query.test(avtale)).isTrue();
    }

    @Test
    void deltaker_oppgitt() {
        Avtale avtale = TestData.enAvtale();
        AvtalePredicate query = new AvtalePredicate();
        query.setDeltakerFnr(avtale.getDeltakerFnr());
        assertThat(query.test(avtale)).isTrue();
    }

    @Test
    void bedrift_oppgitt() {
        Avtale avtale = TestData.enAvtale();
        AvtalePredicate query = new AvtalePredicate();
        query.setBedriftNr(avtale.getBedriftNr());
        assertThat(query.test(avtale)).isTrue();
    }

    @Test
    void kombinasjon_med_feil() {
        Avtale avtale = TestData.enAvtale();
        AvtalePredicate query = new AvtalePredicate();
        query.setVeilederNavIdent(avtale.getVeilederNavIdent());
        query.setBedriftNr(avtale.getBedriftNr());
        query.setDeltakerFnr(new Fnr("66666666666"));
        assertThat(query.test(avtale)).isFalse();
    }

    @Test
    void ingenting_oppgitt() {
        AvtalePredicate query = new AvtalePredicate();
        assertThat(query.test(TestData.enAvtale())).isTrue();
    }
}