package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.stereotype.Component;

@Component
@Data
public class GosysVarsel {
    private final OppgaveVarselService oppgaveVarselService;
    private final PersondataService persondataService;

    private void varsleGosys (Fnr fnr, Tiltakstype tiltakstype) {
        final String aktørid = persondataService.hentAktørId(fnr);
        oppgaveVarselService.opprettOppgave(aktørid, tiltakstype);
    }

}
