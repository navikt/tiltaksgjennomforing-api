package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacAdapter;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TilgangskontrollServiceImpl implements TilgangskontrollService {

  private final AbacAdapter abacAdapter;

  public boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr) {
    return sjekkTilgang(navIdent, fnr);
  }

  private void harTilgang(NavIdent navIdent, Fnr fnr) {
    if (!sjekkTilgang(navIdent, fnr)) {
      throw new IkkeTilgangTilDeltakerException();
    }
  }

  public boolean sjekkTilgang(NavIdent navIdent, Fnr fnr) {
    return abacAdapter.harLeseTilgang(navIdent, fnr);
  }

}