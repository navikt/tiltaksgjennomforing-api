package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;

@Value
public class GodkjennTilskuddsperiodeRequest {
    String enhet;
    Feilkode bekreftetFeilkode;
}
