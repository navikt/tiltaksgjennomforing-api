package no.nav.tag.tiltaksgjennomforing.datavarehus;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;


public class DvhMeldingFilterTest {

    @Test
    public void kun_spesifiserte_tiltakstyper_blir_sendt_til_datavarehus() {
        DvhMeldingProperties dvhMeldingProperties = new DvhMeldingProperties();
        dvhMeldingProperties.setFeatureEnabled(true);
        dvhMeldingProperties.setTiltakstyper(EnumSet.of(Tiltakstype.SOMMERJOBB, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        DvhMeldingFilter dvhMeldingFilter = new DvhMeldingFilter(dvhMeldingProperties);

        Avtale midlertidigLonnstilskuddAvtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        Avtale varigLonnstilskuddAvtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD, true);
        Avtale arbeidstreningAvtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(varigLonnstilskuddAvtale);
        Veileder veileder = TestData.enVeileder(midlertidigLonnstilskuddAvtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), varigLonnstilskuddAvtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), varigLonnstilskuddAvtale);

        assertThat(dvhMeldingFilter.skalTilDatavarehus(midlertidigLonnstilskuddAvtale)).isTrue();
        assertThat(dvhMeldingFilter.skalTilDatavarehus(varigLonnstilskuddAvtale)).isFalse();
        assertThat(dvhMeldingFilter.skalTilDatavarehus(arbeidstreningAvtale)).isFalse();

    }

}