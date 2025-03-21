package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

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
