package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;

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

    public static BjelleVarsel nyttVarsel(Identifikator identifikator,
                                          VarslbarHendelseType hendelseType) {
        BjelleVarsel varsel = new BjelleVarsel();
        varsel.identifikator = identifikator;
        varsel.varslingstekst = hendelseType.getTekst();
        return varsel;
    }

    public void settId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
