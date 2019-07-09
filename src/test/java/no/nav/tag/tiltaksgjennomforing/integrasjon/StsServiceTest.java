package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.StsProperties;
import no.nav.tag.tiltaksgjennomforing.integrasjon.sts.StsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@DirtiesContext
public class StsServiceTest {

    private StsService stsService;

    @Autowired
    StsProperties stsProperties;

    @Autowired
    public void setStsService(StsService stsService) {
        this.stsService = stsService;
    }

    @Test
    public void henter_token() {
        String token = stsService.hentToken();
        assertEquals("eyxXxx", token);
    }
}
