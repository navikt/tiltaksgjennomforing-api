package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

public interface TilgangskontrollService {
  boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr);
  boolean sjekkTilgang(NavIdent navIdent, Fnr fnr);
}
