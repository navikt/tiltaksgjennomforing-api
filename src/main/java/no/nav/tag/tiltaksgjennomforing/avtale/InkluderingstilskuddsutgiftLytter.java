package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddsutgifterFjernet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InkluderingstilskuddsutgiftLytter {
    private final InkluderingstilskuddsutgiftRepository inkluderingstilskuddsutgiftRepository;

    @EventListener
    public void utgifterFjernet(InkluderingstilskuddsutgifterFjernet event) {
        inkluderingstilskuddsutgiftRepository.slettUtgifterUtenKobling(event.getUtgiftIder());
    }
}
