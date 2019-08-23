package no.nav.tag.tiltaksgjennomforing.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.BjelleVarsel;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.BjelleVarselService;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelseRepository;
import no.nav.tag.tiltaksgjennomforing.integrasjon.InnloggingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Protected
@RestController
@RequestMapping("/varsel")
@Timed
@RequiredArgsConstructor
public class BjelleVarselController {
    private final InnloggingService innloggingService;
    private final BjelleVarselService bjelleVarselService;

    @GetMapping
    public Iterable<BjelleVarsel> hentAlleVarsler() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        List<Avtale> avtaler = new ArrayList<>();
        return bjelleVarselService.mineBjelleVarsler(bruker);
    }

    @GetMapping("uleste")
    public Iterable<BjelleVarsel> hentAlleUlesteVarsler() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        List<Avtale> avtaler = new ArrayList<>();
        return bjelleVarselService.mineUlesteBjelleVarsler(bruker);
    }
}
