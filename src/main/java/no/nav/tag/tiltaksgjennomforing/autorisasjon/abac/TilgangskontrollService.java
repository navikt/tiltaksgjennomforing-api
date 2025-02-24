package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.PoaoTilgangService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgangsattributter;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TilgangskontrollService {
    private final PoaoTilgangService poaoTilgangService;

    public TilgangskontrollService(PoaoTilgangService poaoTilgangService) {
        this.poaoTilgangService = poaoTilgangService;
    }

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

    public Map<Fnr, Boolean> harSkrivetilgangTilAvtaler(InternBruker internBruker, List<Avtale> avtaler) {
        Set<Fnr> fnrSet = avtaler.stream().map(Avtale::getDeltakerFnr).collect(Collectors.toSet());
        return harSkrivetilgangTilKandidater(internBruker, fnrSet);
    }

    public Map<Fnr, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Fnr> fnrSet) {
        try {
            return poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), fnrSet);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Collections.emptyMap();
        }
    }

    public Optional<Tilgangsattributter> hentTilgangsattributter(Fnr fnr) {
        try {
            return poaoTilgangService.hentTilgangsattributter(fnr);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Optional.empty();
        }
    }
}
