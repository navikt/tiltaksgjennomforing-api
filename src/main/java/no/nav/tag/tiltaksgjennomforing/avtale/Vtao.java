package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
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
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    public Vtao(Vtao vtao, AvtaleInnhold avtaleInnhold) {
        id = UUID.randomUUID();
        this.fadderFornavn = vtao.fadderFornavn;
        this.fadderEtternavn = vtao.fadderEtternavn;
        this.fadderTlf = vtao.fadderTlf;
        this.avtaleInnhold = avtaleInnhold;
    }

    public Vtao() {
    }
}
