package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvtaleForkortetLytter {
    private final AvtaleForkortetRepository avtaleForkortetRepository;

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvVeileder event) {
        avtaleForkortetRepository.save(new AvtaleForkortetEntitet(
            event.getAvtale(),
            event.getAvtaleInnhold(),
            event.getUtførtAv(),
            event.getNySluttDato(),
            event.getForkortetGrunn().getGrunn(),
            event.getForkortetGrunn().getAnnetGrunn()
        ));
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvArena event) {
        avtaleForkortetRepository.save(new AvtaleForkortetEntitet(
            event.getAvtale(),
            event.getAvtaleInnhold(),
            Identifikator.ARENA,
            event.getNySluttDato(),
            event.getForkortetGrunn().getGrunn(),
            event.getForkortetGrunn().getAnnetGrunn()
        ));
    }
}
