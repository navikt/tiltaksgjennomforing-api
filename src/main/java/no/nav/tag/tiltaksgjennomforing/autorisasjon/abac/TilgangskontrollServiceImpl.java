package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacAdapter;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.InternBruker;

@Service
@RequiredArgsConstructor
public class TilgangskontrollServiceImpl implements TilgangskontrollService {

    private final AbacAdapter abacAdapter;

    public boolean harSkrivetilgangTilKandidat(InternBruker internBruker, Fnr fnr) {
        return abacAdapter.harLeseTilgang(internBruker.getNavIdent().asString(), fnr.asString());
    }

    public Map<Fnr, Boolean> skriveTilganger(InternBruker internBruker, Set<Fnr> fnrListe) {
        return fnrListe.stream()
                .map(fnr -> Map.entry(fnr, harSkrivetilgangTilKandidat(internBruker, fnr)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}