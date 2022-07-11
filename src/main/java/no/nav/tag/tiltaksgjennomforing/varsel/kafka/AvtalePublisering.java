package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

@Data
@AllArgsConstructor
public class AvtalePublisering {
    AvtalePubliseringsType avtalePubliseringsType;
    Avtale avtale;
}
