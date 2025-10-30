package no.nav.tag.tiltaksgjennomforing.avtale.regelmotor;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Regelmotor {
    private final List<IRegel> rules;

    public Regelmotor(List<IRegel> regler) {
        this.rules = regler;
    }

    public List<String> vurder(Avtale avtale){
        return rules.stream()
            .filter(rule -> rule.vurder(avtale))
            .map(IRegel::beskrivelse)
            .collect(Collectors.toList());
    }
}
