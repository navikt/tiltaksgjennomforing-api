package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:smsVarselRepositoryTest"})
@ActiveProfiles("dev")
public class SmsVarselRepositoryTest {
    @Autowired
    private SmsVarselRepository repository;

    @Test
    public void antallUsendteSmsVarsler__teller_riktig() {
        assertThat(repository.antallUsendte()).isEqualTo(0);
        Avtale avtale = TestData.enAvtale();
        avtale.settIdOgOpprettetTidspunkt();
        SmsVarsel smsVarsel = SmsVarsel.nyttVarsel("tlf", new Identifikator("id"), "mld");
        repository.save(smsVarsel);
        assertThat(repository.antallUsendte()).isEqualTo(1);
    }
}