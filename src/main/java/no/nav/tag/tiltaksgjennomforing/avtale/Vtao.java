package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Vtao {
    @Id
    @GeneratedValue
    private UUID id;
    private String fadderFornavn;
    private String fadderEtternavn;
    private String fadderTlf;
    @OneToOne
    @JoinColumn(name = "avtale_innhold_id")
    @JsonIgnore
    private AvtaleInnhold avtaleInnhold;

}
