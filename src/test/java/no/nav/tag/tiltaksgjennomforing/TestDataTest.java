package no.nav.tag.tiltaksgjennomforing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDataTest {
    @Test
    void endringPåAlleFelterSkalIkkeHaNullFelter() {
        assertThat(TestData.endringPåAlleFelter()).hasNoNullFieldsOrProperties();
    }
}