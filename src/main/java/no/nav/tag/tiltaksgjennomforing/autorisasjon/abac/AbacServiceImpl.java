package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacAdapter;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.springframework.stereotype.Service;

@Service
public class AbacServiceImpl implements AbacService{

  private AbacAdapter abacAdapter;

  @Override
  public boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr) {
    return abacAdapter.harLeseTilgang(navIdent,fnr);
  }
}
