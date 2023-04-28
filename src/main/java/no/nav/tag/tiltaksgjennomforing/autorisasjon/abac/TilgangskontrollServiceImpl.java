package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacAdapter;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TilgangskontrollServiceImpl implements TilgangskontrollService {

    private final AbacAdapter abacAdapter;

    public boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr) {
        return abacAdapter.harLeseTilgang(navIdent.asString(), fnr.asString());
    }

    @Override
    public Map<Fnr, Boolean> skriveTilganger(NavIdent navIdent, Set<Fnr> fnrListe) {
        return fnrListe.stream()
                .map(fnr -> Map.entry(fnr, harSkrivetilgangTilKandidat(navIdent, fnr)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}