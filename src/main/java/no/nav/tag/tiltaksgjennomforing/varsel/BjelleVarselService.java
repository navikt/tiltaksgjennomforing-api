package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BjelleVarselService {
    private final BjelleVarselRepository bjelleVarselRepository;

    public List<BjelleVarsel> varslerForInnloggetBruker(InnloggetBruker<?> innloggetBruker, UUID avtaleId, Boolean lest) {
        Stream<BjelleVarsel> stream = bjelleVarslerForInnloggetBruker(innloggetBruker);
        if (avtaleId != null) {
            stream = stream.filter(b -> b.getAvtaleId().equals(avtaleId));
        }
        if (lest != null) {
            stream = stream.filter(b -> b.isLest() == lest);
        }
        return stream.collect(Collectors.toList());
    }

    public void settTilLest(InnloggetBruker<?> innloggetBruker, UUID varselId) {
        bjelleVarslerForInnloggetBruker(innloggetBruker)
                .filter(b -> b.getId().equals(varselId))
                .forEach(b -> {
                    b.settTilLest();
                    bjelleVarselRepository.save(b);
                });
    }

    public void settVarslerTilLest(InnloggetBruker<?> innloggetBruker, List<UUID> varselIder) {
        bjelleVarslerForInnloggetBruker(innloggetBruker)
                .filter(b -> erIListe(b.getId(), varselIder))
                .forEach(b -> {
                    b.settTilLest();
                    bjelleVarselRepository.save(b);
                });
    }

    private boolean erIListe(UUID id, List<UUID> liste) {
        return liste.contains(id);
    }

    private Stream<BjelleVarsel> bjelleVarslerForInnloggetBruker(InnloggetBruker<?> innloggetBruker) {
        return bjelleVarselRepository.findAllByTidspunktAfter(LocalDateTime.now().minusDays(1)).stream()
                .filter(bjelleVarsel -> innloggetBruker.identifikatorer().contains(bjelleVarsel.getIdentifikator()))
                .sorted(Comparator.comparing(BjelleVarsel::getTidspunkt).reversed());
    }
}
