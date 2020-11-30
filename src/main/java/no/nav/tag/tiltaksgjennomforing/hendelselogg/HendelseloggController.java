package no.nav.tag.tiltaksgjennomforing.hendelselogg;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Protected
@RestController
@RequestMapping("/hendelselogg")
@Timed
@RequiredArgsConstructor
public class HendelseloggController {
    private final HendelseloggRepository hendelseloggRepository;
    private final InnloggingService innloggingService;
    private final AvtaleRepository avtaleRepository;

    @GetMapping
    public List<Hendelselogg> hentHendelseloggForAvtale(
            @RequestParam("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        return avtalepart.hentHendelselogg(avtaleId, avtaleRepository, hendelseloggRepository);
    }
}
