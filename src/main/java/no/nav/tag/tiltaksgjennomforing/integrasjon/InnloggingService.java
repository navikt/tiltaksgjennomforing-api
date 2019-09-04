package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_tilgangsstyring.AltinnTilgangsstyringService;
import org.springframework.stereotype.Component;

@Component
public class InnloggingService {
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;

    public InnloggingService(TokenUtils tokenUtils, AltinnTilgangsstyringService altinnTilgangsstyringService) {
        this.tokenUtils = tokenUtils;
        this.altinnTilgangsstyringService = altinnTilgangsstyringService;
    }

    public InnloggetBruker hentInnloggetBruker() {
        if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = tokenUtils.hentInnloggetSelvbetjeningBruker();
            innloggetSelvbetjeningBruker.setOrganisasjoner(altinnTilgangsstyringService.hentOrganisasjoner(innloggetSelvbetjeningBruker.getIdentifikator()));
            return innloggetSelvbetjeningBruker;
        } else {
            return tokenUtils.hentInnloggetNavAnsatt();
        }
    }

    public InnloggetNavAnsatt hentInnloggetNavAnsatt() {
        return tokenUtils.hentInnloggetNavAnsatt();
    }
}
