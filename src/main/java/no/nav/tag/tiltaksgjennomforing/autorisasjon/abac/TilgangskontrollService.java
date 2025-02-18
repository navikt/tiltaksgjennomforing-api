package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TilgangskontrollService {
  boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Fnr fnr);

  Map<Fnr, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Fnr> fnr);

  Optional<String> hentGrunnForAvslag(UUID internBruker, Fnr fnr);
}
