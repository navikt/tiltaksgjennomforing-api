package no.nav.tag.tiltaksgjennomforing.refusjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddPeriodeGodkjent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
public class TilskuddHendelse {

    private final RefusjonProducer refusjonProducer;

    @TransactionalEventListener
    public void godkjentAvVeileder(TilskuddPeriodeGodkjent event) {
        refusjonProducer.publiserRefusjonsmelding(Refusjonsmelding.fraAvtale(event.getAvtale()));
    }
}
