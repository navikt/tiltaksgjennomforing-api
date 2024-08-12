package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;

import java.util.Map;
import java.util.Set;

public interface TilgangskontrollService {
  boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Fnr fnr);

  Map<Fnr, Boolean> skriveTilganger(InternBruker internBruker, Set<Fnr> fnr);
}
