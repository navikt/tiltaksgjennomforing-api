package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.NavEnhet;
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
    public void hentEnheter__returnerer__enheter() {
        List<NavEnhet> enheter = axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent("X123456"));
        assertThat(enheter).isNotEmpty();
    }

    @Test
    public void hentEnheter__returnerer__riktig__enhet() {
        List<NavEnhet> enheter = axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent("X123456"));
        assertThat(enheter).contains(new NavEnhet("0906"));
    }

    @Test
    public void hentEnheter__ugyldig__ident__skal__ikke__ha__enheter() {
        List<NavEnhet> enheter = axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent("X999999"));
        assertThat(enheter).isEmpty();
    }

}
