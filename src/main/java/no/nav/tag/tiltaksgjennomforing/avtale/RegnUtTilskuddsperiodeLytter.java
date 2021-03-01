package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.avtaleperiode.AvtalePeriode;
import no.nav.tag.tiltaksgjennomforing.avtaleperiode.AvtalePeriodeRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegnUtTilskuddsperiodeLytter {
    private final AvtalePeriodeRepository avtalePeriodeRepository;

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        Avtale avtale = event.getAvtale();
        if (harAllePåkrevdeFeltForRegneUtTilskuddsperiode(avtale)) {
            List<AvtalePeriode> avtaleperioder = avtalePeriodeRepository.findAllByAvtaleId(avtale.getId());

        } else {

        }
    }

    private boolean harAllePåkrevdeFeltForRegneUtTilskuddsperiode(Avtale avtale) {
        return avtale.getSumLonnstilskudd() != null && avtale.getStartDato() != null && avtale.getSluttDato() != null;
    }
}
