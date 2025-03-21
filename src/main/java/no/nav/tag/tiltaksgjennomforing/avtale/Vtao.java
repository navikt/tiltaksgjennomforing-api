package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Data
@Entity
@FieldNameConstants
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

    public Vtao() {
    }

    public Vtao(Vtao vtao) {
        id = UUID.randomUUID();
        this.fadderFornavn = vtao.fadderFornavn;
        this.fadderEtternavn = vtao.fadderEtternavn;
        this.fadderTlf = vtao.fadderTlf;
        this.avtaleInnhold = null;
    }

    public Vtao(VtaoFelter vtao, AvtaleInnhold avtaleInnhold) {
        id = UUID.randomUUID();
        this.fadderFornavn = vtao.fadderFornavn();
        this.fadderEtternavn = vtao.fadderEtternavn();
        this.fadderTlf = vtao.fadderTlf();
        this.avtaleInnhold = avtaleInnhold;
    }

    public VtaoFelter hentFelter() {
        return new VtaoFelter(fadderFornavn, fadderEtternavn, fadderTlf);
    }
}
