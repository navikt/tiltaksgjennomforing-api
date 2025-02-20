package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.PoaoTilgangService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgangsattributter;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TilgangskontrollServiceImpl implements TilgangskontrollService {
    private final PoaoTilgangService poaoTilgangService;

    @Override
    public boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Identifikator id) {
        try {
            return poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), id);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return false;
        }
    }

    @Override
    public Optional<String> hentGrunnForAvslag(UUID ident, Identifikator fnr) {
        try {
            return poaoTilgangService.hentGrunn(ident, fnr);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Optional.empty();
        }
    }

    @Override
    public Map<Identifikator, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Identifikator> idSet) {
        try {
            return poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), idSet);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Optional<Tilgangsattributter> hentTilgangsattributter(Identifikator id) {
        try {
            return poaoTilgangService.hentTilgangsattributter(id);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Optional.empty();
        }
    }
}
