package no.nav.tag.tiltaksgjennomforing.datadeling;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "avtale_melding")
public class AvtaleMeldingEntitet extends AbstractAggregateRoot<AvtaleMeldingEntitet> {

    @Id
    private UUID meldingId;
    private UUID avtaleId;
    @Enumerated(EnumType.STRING)
    private HendelseType hendelseType;
    private LocalDateTime tidspunkt;
    private String json;
    private boolean sendt;

    public AvtaleMeldingEntitet(UUID meldingId, UUID avtaleId, LocalDateTime tidspunkt, HendelseType hendelseType, String meldingAsJson) {
        this.meldingId = meldingId;
        this.avtaleId = avtaleId;
        this.hendelseType = hendelseType;
        this.tidspunkt = tidspunkt;
        this.json = meldingAsJson;

        registerEvent(new AvtaleMeldingOpprettet(this));
    }

}
