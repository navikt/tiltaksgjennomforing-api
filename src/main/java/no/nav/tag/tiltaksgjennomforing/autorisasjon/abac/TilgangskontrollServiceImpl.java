package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacAction;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacAdapter;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TilgangskontrollServiceImpl implements TilgangskontrollService {

  private AbacAdapter abacAdapter;

  public boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr) {
    return sjekkTilgang(navIdent, fnr, AbacAction.update);
  }

  private void harTilgang(NavIdent navIdent, Fnr fnr, AbacAction action) {
    if (!sjekkTilgang(navIdent, fnr, action)) {
      throw new IkkeTilgangTilDeltakerException();
    }
  }

  public boolean sjekkTilgang(NavIdent navIdent, Fnr fnr, AbacAction action) {
    return abacAdapter.harLeseTilgang(navIdent, fnr, action);
  }

}