package no.nav.tag.tiltaksgjennomforing.arena.models.arena;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArenaTiltaksgjennomforingIdDeltakerIdOgFnr {
    @Id
    private Integer deltakerId;
    private Integer tiltaksgjennomforingId;
    private String fnr;
}
