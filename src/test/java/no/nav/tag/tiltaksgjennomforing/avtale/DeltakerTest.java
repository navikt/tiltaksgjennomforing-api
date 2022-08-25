package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeltakerTest {
    private AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
    @Test
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        assertThatThrownBy(() -> deltaker.opphevGodkjenninger(avtale)).isInstanceOf(KanIkkeOppheveException.class);
    }

    @Test
    public void mentorAvtale__skjul_mentor_fnr_for_deltaker() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        when(avtaleRepository.findAllByDeltakerFnr(any())).thenReturn(List.of(avtale));

        List<Avtale> avtaler = deltaker.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);
        assertThat(avtaler.get(0).getMentorFnr()).isNull();

    }
}