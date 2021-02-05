package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.IdentifikatorConverter;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselOpprettet;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselResultatMottatt;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class SmsVarsel extends AbstractAggregateRoot<SmsVarsel> {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private SmsVarselStatus status;
    private String telefonnummer;
    @Convert(converter = IdentifikatorConverter.class)
    private Identifikator identifikator;
    private String meldingstekst;
    private UUID varslbarHendelse;

    public static SmsVarsel nyttVarsel(String telefonnummer,
                                       Identifikator identifikator,
                                       String meldingstekst,
                                       UUID varslbarHendelseId) {
        SmsVarsel varsel = new SmsVarsel();
        varsel.id = UUID.randomUUID();
        varsel.status = SmsVarselStatus.USENDT;
        varsel.telefonnummer = telefonnummer;
        varsel.identifikator = identifikator;
        varsel.meldingstekst = meldingstekst;
        varsel.varslbarHendelse = varslbarHendelseId;
        varsel.registerEvent(new SmsVarselOpprettet(varsel));
        return varsel;
    }

    public void endreStatus(SmsVarselStatus status) {
        this.setStatus(status);
        registerEvent(new SmsVarselResultatMottatt(this));
    }
}
