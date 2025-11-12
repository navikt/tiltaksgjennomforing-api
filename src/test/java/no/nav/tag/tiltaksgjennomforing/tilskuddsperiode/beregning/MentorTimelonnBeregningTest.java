package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype.DAGSLØNN;
import static no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype.MÅNEDSLØNN;
import static no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype.TIMELØNN;
import static no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype.UKELØNN;
import static no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype.ÅRSLØNN;
import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MentorTimelonnBeregning.beregnMentorTimelonn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class MentorTimelonnBeregningTest {

    @Test
    void beregetAarsloenn() {
        assertThat(beregnMentorTimelonn(ÅRSLØNN, 780000, BigDecimal.valueOf(100))).isEqualTo(400);
        assertThat(beregnMentorTimelonn(ÅRSLØNN, 780000, BigDecimal.valueOf(50))).isEqualTo(800);
        assertThat(beregnMentorTimelonn(ÅRSLØNN, 780000, BigDecimal.ZERO)).isZero();
        assertThat(beregnMentorTimelonn(ÅRSLØNN, 0, BigDecimal.valueOf(1000))).isZero();
    }

    @Test
    void beregetMaanedsloenn() {
        assertThat(beregnMentorTimelonn(MÅNEDSLØNN, 65000, BigDecimal.valueOf(100))).isEqualTo(400);
        assertThat(beregnMentorTimelonn(MÅNEDSLØNN, 65000, BigDecimal.valueOf(50))).isEqualTo(800);
        assertThat(beregnMentorTimelonn(MÅNEDSLØNN, 65000, BigDecimal.ZERO)).isZero();
        assertThat(beregnMentorTimelonn(MÅNEDSLØNN, 0, BigDecimal.valueOf(100))).isZero();
    }

    @Test
    void beregetUkeloenn() {
        assertThat(beregnMentorTimelonn(UKELØNN, 15000, BigDecimal.valueOf(100))).isEqualTo(400);
        assertThat(beregnMentorTimelonn(UKELØNN, 15000, BigDecimal.valueOf(50))).isEqualTo(800);
        assertThat(beregnMentorTimelonn(UKELØNN, 15000, BigDecimal.ZERO)).isZero();
        assertThat(beregnMentorTimelonn(UKELØNN, 0, BigDecimal.valueOf(100))).isZero();
    }

    @Test
    void beregnetDagsloenn() {
        assertThat(beregnMentorTimelonn(DAGSLØNN, 3000, BigDecimal.valueOf(100))).isEqualTo(400);
        assertThat(beregnMentorTimelonn(DAGSLØNN, 3000, BigDecimal.valueOf(50))).isEqualTo(800);
        assertThat(beregnMentorTimelonn(DAGSLØNN, 3000, BigDecimal.ZERO)).isZero();
        assertThat(beregnMentorTimelonn(DAGSLØNN, 0, BigDecimal.valueOf(100))).isZero();
    }

    @Test
    void beregetTimeloenn() {
        assertThat(beregnMentorTimelonn(TIMELØNN, 400, BigDecimal.valueOf(100))).isEqualTo(400);
        assertThat(beregnMentorTimelonn(TIMELØNN, 400, BigDecimal.valueOf(50))).isEqualTo(400);
        assertThat(beregnMentorTimelonn(TIMELØNN, 400, BigDecimal.ZERO)).isZero();
        assertThat(beregnMentorTimelonn(TIMELØNN, 0, BigDecimal.valueOf(100))).isZero();
    }

    @Test
    void stillingsprosentMindreEnn0kasterException() {
        assertThatException().isThrownBy(() -> beregnMentorTimelonn(TIMELØNN, 400, BigDecimal.valueOf(-1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loennsTypeBelopMindreEnn0kasterException() {
        assertThatException().isThrownBy(() -> beregnMentorTimelonn(TIMELØNN, -1, BigDecimal.valueOf(100))).isInstanceOf(IllegalArgumentException.class);
    }
}