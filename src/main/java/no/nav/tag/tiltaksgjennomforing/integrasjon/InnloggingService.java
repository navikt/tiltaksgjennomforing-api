package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_tilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.SystembrukerProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InnloggingService {

    private final SystembrukerProperties systembrukerProperties;
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;

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

    public void validerSystembruker() {
      if(!tokenUtils.hentInnloggetSystem().equals(systembrukerProperties.getId())) {
          throw new TilgangskontrollException("Systemet har ikke tilgang til tjenesten");
      }

    }
}
