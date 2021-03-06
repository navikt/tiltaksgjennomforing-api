package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsVarselMelding {
    private UUID smsVarselId;
    private Identifikator identifikator;
    private String telefonnummer;
    private String meldingstekst;
}
