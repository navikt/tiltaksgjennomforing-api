package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class AvtaleTest {

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("01234567890");
        Avtale avtale = Avtale.nyAvtale(deltakerFnr);
        assertThat(avtale.getOpprettetTidspunkt()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
        assertThat(avtale.getMaal()).isEmpty();
        assertThat(avtale.getOppgaver()).isEmpty();
        // TODO: Assert alle verdier
    }

}