package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class Maal {
    @Id
    @GeneratedValue
    private UUID id;
    private String kategori;
    private String beskrivelse;
    @ManyToOne
    @JoinColumn(name = "avtale")
    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

    public void sjekkMaalLengde() {
        Utils.sjekkAtTekstIkkeOverskrider1000Tegn(this.getBeskrivelse(), "Maks lengde for mål er 1000 tegn");
    }
}
