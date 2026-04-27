package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Profile({Miljø.DEV_FSS, Miljø.PROD_FSS})
@Component
@RequiredArgsConstructor
class LagGosysVarselLytter {
    private final GosysVarselService gosysVarselService;

    @TransactionalEventListener
    public void opprettGosysVarsel(AvtaleOpprettetAvArbeidsgiver event) {
        gosysVarselService.varsleGosysOmOpprettetAvtale(event.getAvtale());
    }

    @TransactionalEventListener
    public void opprettGosysVarsel(AvtaleOpprettetAvArena event) {
        gosysVarselService.varsleGosysOmOpprettetAvtale(event.getAvtale());
    }

    @TransactionalEventListener
    public void opprettVTAOGosysVarsel(AvtaleInngått event) {
        if (
            Tiltakstype.VTAO.equals(event.getAvtale().getTiltakstype()) &&
            !Avtaleopphav.ARENA.equals(event.getAvtale().getOpphav())
        ) {
            gosysVarselService.varsleGosysOmInngaattVTAOAvtale(event.getAvtale());
        }
    }
}
