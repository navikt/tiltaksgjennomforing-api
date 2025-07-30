package no.nav.tag.tiltaksgjennomforing.datavarehus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "dvh_melding")
public class DvhMeldingEntitet extends AbstractAggregateRoot<DvhMeldingEntitet> {
    @Id
    private UUID meldingId;
    private UUID avtaleId;
    private Instant tidspunkt;
    @Enumerated(EnumType.STRING)
    private Status tiltakStatus;
    private String json;
    private boolean sendt;

    public DvhMeldingEntitet(Avtale avtale, AvroTiltakHendelse avroTiltakHendelse) {
        this.meldingId = UUID.fromString(avroTiltakHendelse.getMeldingId());
        this.avtaleId = avtale.getId();
        this.tidspunkt = avroTiltakHendelse.getTidspunkt();
        this.tiltakStatus = avtale.getStatus();
        this.json = avroTiltakHendelse.toString();
        registerEvent(new DvhMeldingOpprettet(this, avroTiltakHendelse));
    }
}
