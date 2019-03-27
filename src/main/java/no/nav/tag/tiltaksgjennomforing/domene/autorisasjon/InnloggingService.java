package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.controller.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.AltinnService;
import org.springframework.stereotype.Component;

@Component
public class InnloggingService {
    private final TokenUtils tokenUtils;
    private final AltinnService altinnService;

    public InnloggingService(TokenUtils tokenUtils, AltinnService altinnService) {
        this.tokenUtils = tokenUtils;
        this.altinnService = altinnService;
    }

    public InnloggetBruker hentInnloggetBruker() {
        if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = tokenUtils.hentInnloggetSelvbetjeningBruker();
            innloggetSelvbetjeningBruker.getOrganisasjoner().addAll(altinnService.hentOrganisasjoner(innloggetSelvbetjeningBruker.getIdentifikator()));
            return innloggetSelvbetjeningBruker;
        } else if (tokenUtils.erInnloggetNavAnsatt()) {
            return tokenUtils.hentInnloggetNavAnsatt();
        }
        throw new TilgangskontrollException("");
    }

    public InnloggetNavAnsatt hentInnloggetNavAnsatt() {
        return tokenUtils.hentInnloggetNavAnsatt();
    }
}
