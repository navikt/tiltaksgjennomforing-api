package no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_varsel;

import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.Varsel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@DirtiesContext
public class AltinnVarselServiceTest {
    @Autowired
    private AltinnVarselService varselService;

    @Test
    public void sendVarsel() {
        varselService.sendVarsel(new Varsel(TestData.enIdentifikator(), "12345678", "varseltekst"));
    }
}