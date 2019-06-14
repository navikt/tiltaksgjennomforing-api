package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.domene.VarslbarHendelseFactory;
import no.nav.tag.tiltaksgjennomforing.domene.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VarslbarHendelseListener {
    private final VarslbarHendelseProducer producer;

    @EventListener
    public void opprettet(AvtaleOpprettet event) {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleOpprettet(event.getAvtale());
        producer.sendVarslbarHendelse(varslbarHendelse);
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvDeltaker(event.getAvtale());
        producer.sendVarslbarHendelse(varslbarHendelse);
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvArbeidsgiver(event.getAvtale());
        producer.sendVarslbarHendelse(varslbarHendelse);
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.avtaleGodkjentAvVeileder(event.getAvtale());
        producer.sendVarslbarHendelse(varslbarHendelse);
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevet event) {
        VarslbarHendelse varslbarHendelse = VarslbarHendelseFactory.godkjenningerOpphevet(event.getAvtale());
        producer.sendVarslbarHendelse(varslbarHendelse);
    }
}
