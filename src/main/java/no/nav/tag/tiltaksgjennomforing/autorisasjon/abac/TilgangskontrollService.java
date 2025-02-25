package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgangsattributter;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TilgangskontrollService {
  boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Identifikator id);
  Map<Identifikator, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Identifikator> idSet);
  Optional<String> hentGrunnForAvslag(UUID internBruker, Identifikator id);
  Optional<Tilgangsattributter> hentTilgangsattributter(Identifikator id);
}
