package no.nav.tag.tiltaksgjennomforing.domene.prosess;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.IdOgTidspunktGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StatusJournalforing implements IdOgTidspunktGenerator {

    @Id
    protected UUID id;
    private UUID avtale;
    private LocalDateTime tidspunkt;
    private JournalforingStatus status;

    public StatusJournalforing(UUID avtale, JournalforingStatus status){
        this.avtale = avtale;
        this.status = status;
    }

    @Override
    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (tidspunkt == null) {
            tidspunkt = LocalDateTime.now();
        }
    }
}

