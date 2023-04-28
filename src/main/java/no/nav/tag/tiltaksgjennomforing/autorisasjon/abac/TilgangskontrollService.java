package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import java.util.Map;
import java.util.Set;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

public interface TilgangskontrollService {
  boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr);

  Map<Fnr, Boolean> skriveTilganger(NavIdent navIdent, Set<Fnr> fnr);
}
