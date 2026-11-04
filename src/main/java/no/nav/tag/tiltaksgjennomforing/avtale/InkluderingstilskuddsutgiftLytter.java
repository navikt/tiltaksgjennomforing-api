package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InkluderingstilskuddsutgiftLytter {
    private final InkluderingstilskuddsutgiftRepository inkluderingstilskuddsutgiftRepository;

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        if (event.getAvtale().getTiltakstype() == Tiltakstype.INKLUDERINGSTILSKUDD) {
            inkluderingstilskuddsutgiftRepository.slettUtgifterUtenKobling();
        }
    }
}
