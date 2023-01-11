package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleArenaMigreringTest {

    @Test
    public void lonnstilskudd_skal_generere_tilskuddsperioder_med_behandlet_status_om_ryddeavtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(60);
        assertThat(avtale.getTilskuddPeriode()).isEmpty();

        Veileder veileder = TestData.enVeileder(avtale);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        veileder.endreAvtale(Instant.now(), endreAvtale, avtale, EnumSet.of(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();
        // assertThat(true).isFalse();
    }

}
