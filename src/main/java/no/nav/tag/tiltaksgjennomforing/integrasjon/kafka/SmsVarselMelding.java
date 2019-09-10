package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;

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
