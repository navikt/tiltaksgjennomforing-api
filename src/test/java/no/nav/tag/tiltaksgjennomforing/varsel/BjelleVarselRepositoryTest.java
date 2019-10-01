package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class BjelleVarselRepositoryTest {
    @Autowired
    private BjelleVarselRepository bjelleVarselRepository;
    @Autowired
    private AvtaleRepository avtaleRepository;
    private Avtale avtale;

    @Before
    public void setUp() {
        avtale = TestData.enAvtale();
        avtaleRepository.save(avtale);
    }

    @Test
    public void save__lagrer_riktig() {
        BjelleVarsel bjelleVarsel = TestData.etBjelleVarsel(avtale);
        BjelleVarsel lagretBjelleVarsel = bjelleVarselRepository.save(bjelleVarsel);
        assertThat(lagretBjelleVarsel).isEqualToIgnoringNullFields(bjelleVarsel);
    }
}