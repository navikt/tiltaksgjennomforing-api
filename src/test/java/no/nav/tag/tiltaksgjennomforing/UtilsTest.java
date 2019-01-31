package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {
    @Test
    public void erIkkeNull__med_null() {
        assertThat(Utils.erIkkeNull(1, "k", null)).isFalse();
    }

    @Test
    public void erIkkeNull__uten_null() {
        assertThat(Utils.erIkkeNull(1, "k", new Object())).isTrue();
    }
}