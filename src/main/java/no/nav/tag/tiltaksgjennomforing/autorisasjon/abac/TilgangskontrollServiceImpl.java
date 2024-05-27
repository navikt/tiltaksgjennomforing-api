package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.PoaoTilgangService;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TilgangskontrollServiceImpl implements TilgangskontrollService {

    private final PoaoTilgangService poaoTilgangService;

    public boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Fnr fnr) {
        try {
            return poaoTilgangService.harSkriveTilgang(internBruker.getAzureOid(), fnr.asString());
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return false;
        }
    }

    public Map<Fnr, Boolean> skriveTilganger(InternBruker internBruker, Set<Fnr> fnrListe) {
        return fnrListe.stream()
                .map(fnr -> Map.entry(fnr, harSkrivetilgangTilKandidat(internBruker, fnr)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
