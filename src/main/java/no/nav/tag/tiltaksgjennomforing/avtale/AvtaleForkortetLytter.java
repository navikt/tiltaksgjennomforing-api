package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvSystem;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvtaleForkortetLytter {
    private final AvtaleForkortetRepository avtaleForkortetRepository;

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvVeileder event) {
        avtaleForkortetRepository.save(new AvtaleForkortetEntitet(event.getAvtale(), event.getAvtaleInnhold(), event.getUtførtAv(), event.getNySluttDato(), event.getGrunn(), event.getAnnetGrunn()));
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvSystem event) {
        avtaleForkortetRepository.save(new AvtaleForkortetEntitet(event.getAvtale(), event.getAvtaleInnhold(), event.getUtførtAv(), event.getNySluttDato(), event.getGrunn(), null));
    }
}
