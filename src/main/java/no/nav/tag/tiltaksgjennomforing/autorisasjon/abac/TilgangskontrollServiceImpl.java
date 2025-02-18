package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.PoaoTilgangService;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
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

    public boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Fnr fnr) {
        try {
            return poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), fnr);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return false;
        }
    }

    public Optional<String> hentGrunnForAvslag(UUID ident, Fnr fnr) {
        try {
            return poaoTilgangService.hentGrunn(ident, fnr);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Optional.empty();
        }
    }

    public Map<Fnr, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Fnr> fnrListe) {
        try {
            return poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), fnrListe);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Collections.emptyMap();
        }
    }
}
