package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BjelleVarselService {
    private final BjelleVarselRepository bjelleVarselRepository;

    public Iterable<BjelleVarsel> mineBjelleVarsler(InnloggetBruker innloggetBruker) {
        return bjelleVarslerForInnloggetBruker(innloggetBruker)
                .collect(Collectors.toList());
    }

    public Iterable<BjelleVarsel> mineUlesteBjelleVarsler(InnloggetBruker bruker) {
        return bjelleVarslerForInnloggetBruker(bruker)
                .filter(Predicate.not(BjelleVarsel::isLest))
                .collect(Collectors.toList());
    }

    private Stream<BjelleVarsel> bjelleVarslerForInnloggetBruker(InnloggetBruker innloggetBruker) {
        return bjelleVarselRepository.finnAlleForIdentifikator(innloggetBruker.getIdentifikator().asString()).stream();
    }
}
