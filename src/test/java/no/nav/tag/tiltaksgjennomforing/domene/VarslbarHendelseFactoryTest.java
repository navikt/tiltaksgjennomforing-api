package no.nav.tag.tiltaksgjennomforing.domene;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VarslbarHendelseFactoryTest {

    private final Avtale avtale = TestData.enAvtale();

    @Test
    public void avtaleGodkjentAvDeltaker() {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvDeltaker(avtale);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getAvgiver).contains(avtale.getBedriftNr());
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getTelefonnummer).contains(avtale.getVeilederTlf(), avtale.getArbeidsgiverTlf());
    }

    @Test
    public void avtaleGodkjentAvArbeidsgiver() {
    }

    @Test
    public void avtaleGodkjentAvVeileder() {
    }

    @Test
    public void godkjenningerOpphevet() {
    }
}