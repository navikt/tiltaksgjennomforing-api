package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class GodkjentPaVegneGrunn {
    @Id
    @Column(name = "avtale")
    @JsonIgnore
    private UUID id;

    @OneToOne
    @JoinColumn(name = "avtale")
    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

    private boolean ikkeBankId;
    private boolean reservert;
    private boolean digitalKompetanse;

    public void valgtMinstEnGrunn() {
        if (!ikkeBankId && !reservert && !digitalKompetanse) {
            throw new TiltaksgjennomforingException("Minst èn grunn må være valgt");
        }
    }
}

