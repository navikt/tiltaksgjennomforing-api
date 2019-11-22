package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
public class Oppgave {
    @Id
    @GeneratedValue
    private UUID id;
    private String tittel;
    private String beskrivelse;
    private String opplaering;
    @ManyToOne
    @JoinColumn(name = "avtale_innhold")
    @JsonIgnore
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    public Oppgave() {
    }

    public Oppgave(Oppgave fra) {
        id = UUID.randomUUID();
        tittel = fra.tittel;
        beskrivelse = fra.beskrivelse;
        opplaering = fra.opplaering;
    }


    public void sjekkOppgaveLengde() {
        Utils.sjekkAtTekstIkkeOverskrider1000Tegn(this.getBeskrivelse(), "Maks lengde for oppgavebeskrivelse er 1000 tegn");
        Utils.sjekkAtTekstIkkeOverskrider1000Tegn(this.getOpplaering(), "Maks lengde for opplæring er 1000 tegn");
    }
}
