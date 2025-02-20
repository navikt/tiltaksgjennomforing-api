package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.PoaoTilgangService;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TilgangskontrollServiceImpl implements TilgangskontrollService {
    private final PoaoTilgangService poaoTilgangService;
    private final PersondataService persondataService;

    public TilgangskontrollServiceImpl(PoaoTilgangService poaoTilgangService, PersondataService persondataService) {
        this.poaoTilgangService = poaoTilgangService;
        this.persondataService = persondataService;
    }

    public boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Fnr fnr) {
        try {
            AktorId aktorId = persondataService.hentAktorId(fnr);
            return poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), aktorId);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return false;
        }
    }

    public Optional<String> hentGrunnForAvslag(UUID ident, Fnr fnr) {
        try {
            AktorId aktorId = persondataService.hentAktorId(fnr);
            return poaoTilgangService.hentGrunn(ident, aktorId);
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Optional.empty();
        }
    }

    public Map<Fnr, Boolean> harSkrivetilgangTilKandidater(InternBruker internBruker, Set<Fnr> fnrSet) {
        try {
            Map<Fnr, AktorId> aktorId = persondataService.hentAktorId(fnrSet);
            Map<AktorId, Boolean> tilgang = poaoTilgangService.harSkrivetilgang(internBruker.getAzureOid(), new HashSet<>(aktorId.values()));

            return aktorId.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> tilgang.getOrDefault(entry.getValue(), false)
            ));
        } catch (Exception e) {
            log.error("Feil ved tilgangskontroll-sjekk", e);
            return Collections.emptyMap();
        }
    }
}
