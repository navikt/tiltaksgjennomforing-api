package no.nav.tag.tiltaksgjennomforing.satser;

import io.micrometer.core.annotation.Timed;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;

@RestController
@Unprotected
@RequestMapping("/satser")
@Timed
public class SatsController {
    @GetMapping("/vtao")
    public SatsResponse getVtaoSats(@RequestParam(value = "forDato", required = false) LocalDate forDato) {
        var dato = Now.localDate();
        if (forDato != null) {
            dato = forDato;
        }
        return new SatsResponse(
                "VTAO",
                dato.getYear(),
                VTAO_SATS.hentGjeldendeSats(dato)
        );
    }
}
