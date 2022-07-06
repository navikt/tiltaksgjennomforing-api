package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvtalePupliseringsEvent {
    String avtalePubliseringsType;
    AvtaleEndret avtaleEndret;
}
