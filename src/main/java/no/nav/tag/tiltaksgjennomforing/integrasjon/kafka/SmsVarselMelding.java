package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.SmsVarsel;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class SmsVarselMelding {
    private UUID smsVarselId;
    private Identifikator avgiver;
    private String telefonnummer;
    private String varseltekst;

    public static SmsVarselMelding create(SmsVarsel smsVarsel) {
        return new SmsVarselMelding(smsVarsel.getId(), smsVarsel.getIdentifikator(), smsVarsel.getTelefonnummer(), smsVarsel.getMeldingstekst());
    }
}
