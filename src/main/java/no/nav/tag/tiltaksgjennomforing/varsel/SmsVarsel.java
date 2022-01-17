package no.nav.tag.tiltaksgjennomforing.varsel;

import java.util.UUID;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.IdentifikatorConverter;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselOpprettet;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselResultatMottatt;
import org.springframework.data.domain.AbstractAggregateRoot;

@Slf4j
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
        log.info("SENDER SMS: {}, {}", varsel.telefonnummer, varsel);
        varsel.registerEvent(new SmsVarselOpprettet(varsel));
        return varsel;
    }

    public void endreStatus(SmsVarselStatus status) {
        this.setStatus(status);
        registerEvent(new SmsVarselResultatMottatt(this));
    }

    public static SmsVarsel nyttVarselForGjeldendeKontaktpersonForRefusjon(Avtale avtale, String meldingstekst,
        UUID varslbarHendelseId){
        return nyttVarsel(avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(), avtale.getBedriftNr(),
            meldingstekst, varslbarHendelseId);
    }
}
