package no.nav.tag.tiltaksgjennomforing.domene;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VarslbarHendelseFactoryTest {

    private final Avtale avtale = TestData.enAvtaleMedAltUtfylt();

    @Test
    public void avtaleGodkjentAvDeltaker() {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvDeltaker(avtale);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getAvgiver).containsOnly(VarslbarHendelseFactory.NAV_ORGNR);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getTelefonnummer).containsOnly(avtale.getVeilederTlf());
    }

    @Test
    public void avtaleGodkjentAvArbeidsgiver() {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvArbeidsgiver(avtale);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getAvgiver).containsOnly(VarslbarHendelseFactory.NAV_ORGNR);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getTelefonnummer).containsOnly(avtale.getVeilederTlf());
    }

    @Test
    public void avtaleGodkjentAvVeileder() {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvVeileder(avtale);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getAvgiver).containsOnly(avtale.getBedriftNr(), avtale.getDeltakerFnr());
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getTelefonnummer).containsOnly(avtale.getArbeidsgiverTlf(), avtale.getDeltakerTlf());
    }

    @Test
    public void godkjenningerOpphevet() {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.godkjenningerOpphevet(avtale);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getAvgiver).containsOnly(avtale.getBedriftNr(), avtale.getDeltakerFnr(), VarslbarHendelseFactory.NAV_ORGNR);
        assertThat(varslbarHendelse.getVarsler()).extracting(Varsel::getTelefonnummer).containsOnly(avtale.getArbeidsgiverTlf(), avtale.getDeltakerTlf(), avtale.getVeilederTlf());
    }
}