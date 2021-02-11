package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleNyVeileder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OvertaVarslerLytter {
    private final VarselRepository varselRepository;

    @EventListener
    public void overtaVarsler(AvtaleNyVeileder event) {
        NavIdent nyVeileder = event.getAvtale().getVeilederNavIdent();
        NavIdent tidligereVeileder = event.getTidligereVeileder();

        List<Varsel> varsler = varselRepository.findAllByAvtaleIdAndIdentifikator(event.getAvtale().getId(), tidligereVeileder);
        varsler.forEach(varsel -> varsel.setIdentifikator(nyVeileder));
        varselRepository.saveAll(varsler);
    }
}
