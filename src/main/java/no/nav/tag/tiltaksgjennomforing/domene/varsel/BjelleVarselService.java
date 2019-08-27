package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BjelleVarselService {
    private final BjelleVarselRepository bjelleVarselRepository;

    public List<BjelleVarsel> mineBjelleVarsler(InnloggetBruker innloggetBruker) {
        return bjelleVarslerForInnloggetBruker(innloggetBruker)
                .collect(Collectors.toList());
    }

    public List<BjelleVarsel> mineUlesteBjelleVarsler(InnloggetBruker bruker) {
        return bjelleVarslerForInnloggetBruker(bruker)
                .filter(Predicate.not(BjelleVarsel::isLest))
                .collect(Collectors.toList());
    }

    public void settTilLest(InnloggetBruker innloggetBruker) {
        List<BjelleVarsel> bjelleVarsler = mineUlesteBjelleVarsler(innloggetBruker);
        bjelleVarsler.forEach(BjelleVarsel::settTilLest);
        bjelleVarselRepository.saveAll(bjelleVarsler);
    }

    private Stream<BjelleVarsel> bjelleVarslerForInnloggetBruker(InnloggetBruker innloggetBruker) {
        return bjelleVarselRepository.findAll().stream()
                .filter(bjelleVarsel -> innloggetBruker.identifikatorer().contains(bjelleVarsel.getIdentifikator()));
    }
}
