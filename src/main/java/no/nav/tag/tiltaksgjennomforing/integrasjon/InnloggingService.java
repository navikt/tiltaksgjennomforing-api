package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.integrasjon.altinn.AltinnService;
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
            innloggetSelvbetjeningBruker.setOrganisasjoner(altinnService.hentOrganisasjoner(innloggetSelvbetjeningBruker.getIdentifikator()));
            return innloggetSelvbetjeningBruker;
        } else {
            return tokenUtils.hentInnloggetNavAnsatt();
        }
    }

    public InnloggetNavAnsatt hentInnloggetNavAnsatt() {
        return tokenUtils.hentInnloggetNavAnsatt();
    }
}
