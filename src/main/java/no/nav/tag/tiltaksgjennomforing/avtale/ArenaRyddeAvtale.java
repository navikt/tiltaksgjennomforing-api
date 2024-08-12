package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class ArenaRyddeAvtale {
    @Id
    private UUID id = UUID.randomUUID();
    @OneToOne
    @JoinColumn(name = "avtale")
    private Avtale avtale;
    private LocalDate migreringsdato;
}
