package no.nav.tag.tiltaksgjennomforing.utils;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {
    @Test
    public void erIkkeTomme__med_null() {
        assertThat(Utils.erIkkeTomme(1, "k", null)).isFalse();
    }

    @Test
    public void erIkkeTomme__med_tom_streng() {
        assertThat(Utils.erIkkeTomme(1, "k", "")).isFalse();
    }

    @Test
    public void erIkkeTomme__uten_null() {
        assertThat(Utils.erIkkeTomme(1, "k", new Object())).isTrue();
    }

    @Test
    void equalsMenIkkeNull() {
        assertThat(Utils.equalsMenIkkeNull(null, null)).isFalse();

        BedriftNr a = new BedriftNr("123456789");
        BedriftNr b = new BedriftNr("123456789");

        assertThat(Utils.equalsMenIkkeNull(null, a)).isFalse();
        assertThat(Utils.equalsMenIkkeNull(b, null)).isFalse();
        assertThat(Utils.equalsMenIkkeNull(a, b)).isTrue();
        assertThat(Utils.equalsMenIkkeNull(b, a)).isTrue();

        BedriftNr c = new BedriftNr("987654321");

        assertThat(Utils.equalsMenIkkeNull(a, c)).isFalse();
        assertThat(Utils.equalsMenIkkeNull(c, b)).isFalse();
    }
}
