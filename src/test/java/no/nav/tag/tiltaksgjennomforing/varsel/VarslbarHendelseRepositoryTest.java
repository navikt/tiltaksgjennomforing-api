package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@DirtiesContext
public class VarslbarHendelseRepositoryTest {
    @Autowired
    private VarslbarHendelseRepository varslbarHendelseRepository;
    @Autowired
    private AvtaleRepository avtaleRepository;

    @Test
    public void save__lagrer_alle_felter() {
        Avtale avtale = TestData.enAvtale();
        avtaleRepository.save(avtale);
        VarslbarHendelse varslbarHendelse = TestData.enHendelse(avtale);
        VarslbarHendelse lagretVarslbarHendelse = varslbarHendelseRepository.save(varslbarHendelse);
        assertThat(lagretVarslbarHendelse.getId()).isNotNull();
        assertThat(lagretVarslbarHendelse.getTidspunkt()).isNotNull();
        assertThat(lagretVarslbarHendelse).isEqualToIgnoringNullFields(varslbarHendelse);
    }
}