package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelseRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:varslbarHendelseRepositoryTest"})
@ActiveProfiles("dev")
public class VarslbarHendelseRepositoryTest {
    @Autowired
    private VarslbarHendelseRepository varslbarHendelseRepository;

    @Before
    public void setUp() {
        varslbarHendelseRepository.deleteAll();
    }

    @Test
    public void save__lagrer_alle_felter() {
        Avtale avtale = TestData.enAvtale();
        avtale.settIdOgOpprettetTidspunkt();
        VarslbarHendelse varslbarHendelse = TestData.enHendelseMedSmsVarsel(avtale);
        VarslbarHendelse lagretVarslbarHendelse = varslbarHendelseRepository.save(varslbarHendelse);
        assertThat(lagretVarslbarHendelse.getId()).isNotNull();
        assertThat(lagretVarslbarHendelse.getTidspunkt()).isNotNull();
        assertThat(lagretVarslbarHendelse.getVarslbarHendelseType()).isNotNull().isEqualTo(varslbarHendelse.getVarslbarHendelseType());
        assertThat(lagretVarslbarHendelse.getAvtaleId()).isNotNull().isEqualTo(varslbarHendelse.getAvtaleId());
        assertThat(lagretVarslbarHendelse.getSmsVarsler()).containsAll(varslbarHendelse.getSmsVarsler());
    }

    @Test
    public void antallUsendteSmsVarsler__teller_riktig() {
        assertThat(varslbarHendelseRepository.antallUsendteSmsVarsler()).isEqualTo(0);
        Avtale avtale = TestData.enAvtale();
        avtale.settIdOgOpprettetTidspunkt();
        VarslbarHendelse varslbarHendelse = TestData.enHendelseMedSmsVarsel(avtale);
        varslbarHendelseRepository.save(varslbarHendelse);
        assertThat(varslbarHendelseRepository.antallUsendteSmsVarsler()).isEqualTo(1);
    }
}