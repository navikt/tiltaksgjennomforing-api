package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.PoaoTilgangService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
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
            return hentSkrivetilgang(internBruker, fnr).erTillat();
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return false;
        }
    }

    public Tilgang hentSkrivetilgang(InternBruker internBruker, Fnr fnr) {
        return hentSkrivetilgang(internBruker.getAzureOid(), fnr);
    }

    public Tilgang hentSkrivetilgang(UUID azureOid, Fnr fnr) {
        try {
            return poaoTilgangService
                .hentSkrivetilgang(azureOid, fnr)
                .orElse(new Tilgang.Avvis(Avslagskode.INGEN_RESPONS, "Tilgang mangler fra POAO-tilgang"));
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return new Tilgang.Avvis(Avslagskode.INGEN_RESPONS, "Ingen respons fra POAO-tilgang");
        }
    }

    public Map<Fnr, Boolean> harSkrivetilgangTilAvtaler(InternBruker internBruker, List<Avtale> avtaler) {
        Set<Fnr> fnrSet = avtaler.stream().map(Avtale::getDeltakerFnr).collect(Collectors.toSet());
        return harSkrivetilgangTilKandidater(internBruker, fnrSet);
    }

    public Map<Fnr, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Fnr> fnrSet) {
        try {
            return poaoTilgangService.harSkrivetilganger(internBruker.getAzureOid(), fnrSet);
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
