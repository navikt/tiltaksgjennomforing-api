package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.axsys.AxsysService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@DirtiesContext
public class AxsysServiceTest {
    @Autowired
    private AxsysService axsysService;

    @Test
    public void hentIdenter__returnerer__riktig__ident() {
        List<NavIdent> identer = axsysService.hentIdenter("0906");
        assertThat(identer).contains(new NavIdent("Z123456"));
    }

    @Test
    public void hentIdenter__returnerer__identer() {
        List<NavIdent> identer = axsysService.hentIdenter("0906");
        assertThat(identer).isNotEmpty();
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void hentIdenter__ugyldig__kontorId__kaster__feil() {
        List<NavIdent> identer = axsysService.hentIdenter("09060");
    }

}
