package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselStatus;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class SmsVarselResultatMelding {
    private UUID smsVarselId;
    private SmsVarselStatus status;
}
