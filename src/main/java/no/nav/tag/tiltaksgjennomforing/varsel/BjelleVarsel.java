package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BjelleVarsel extends AbstractAggregateRoot {
    @Id
    private UUID id;
    private boolean lest;
    private Identifikator identifikator;
    private String varslingstekst;
    private UUID varslbarHendelse;
    private UUID avtaleId;
    private LocalDateTime tidspunkt;

    public static BjelleVarsel nyttVarsel(Identifikator identifikator, VarslbarHendelse varslbarHendelse) {
        BjelleVarsel varsel = new BjelleVarsel();
        varsel.identifikator = identifikator;
        varsel.varslbarHendelse = varslbarHendelse.getId();
        varsel.varslingstekst = varslbarHendelse.getVarslbarHendelseType().getTekst();
        varsel.avtaleId = varslbarHendelse.getAvtaleId();
        return varsel;
    }

    public void settTilLest() {
        lest = true;
    }

    public void settId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (tidspunkt == null) {
            tidspunkt = LocalDateTime.now();
        }
    }
}
