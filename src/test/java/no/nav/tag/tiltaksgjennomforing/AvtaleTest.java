package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class AvtaleTest {

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        String deltakerFodselsnr = "012334567890";
        Avtale avtale = Avtale.nyAvtale(deltakerFodselsnr);
        assertThat(avtale.getOpprettetTidspunkt()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(avtale.getDeltakerFodselsnr()).isEqualTo(deltakerFodselsnr);
        assertThat(avtale.getMaal()).isEmpty();
        assertThat(avtale.getOppgaver()).isEmpty();
        // TODO: Assert alle verdier
    }

}