package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMentorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MENTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MentorStrategyTest {

    private AvtaleInnhold avtaleInnhold;
    private AvtaleInnholdStrategy strategy;

    @BeforeEach
    public void setUp() {
        avtaleInnhold = new AvtaleInnhold();
        strategy = AvtaleInnholdStrategyFactory.create(avtaleInnhold, MENTOR);
    }

    @Test
    public void endreMentortilskudd__startdato_er_etter_sluttdato() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertThatThrownBy(() -> strategy.endre(endreAvtale)).isInstanceOf(StartDatoErEtterSluttDatoException.class);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_36mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(36);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        strategy.endre(endreAvtale);

        assertThat(strategy.erAltUtfylt()).isTrue();
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_36mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(36).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertThatThrownBy(() -> strategy.endre(endreAvtale)).isInstanceOf(VarighetForLangMentorException.class);
    }
}
