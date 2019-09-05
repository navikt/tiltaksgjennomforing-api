package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Oppgave {
    @Id
    private UUID id;
    private LocalDateTime opprettetTidspunkt;
    private String tittel;
    private String beskrivelse;
    private String opplaering;

    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (opprettetTidspunkt == null) {
            opprettetTidspunkt = LocalDateTime.now();
        }
    }

    public void sjekkOppgaveLengde() {
        Utils.sjekkAtTekstIkkeOverskrider1000Tegn(this.getBeskrivelse(), "Maks lengde for oppgavebeskrivelse er 1000 tegn");
        Utils.sjekkAtTekstIkkeOverskrider1000Tegn(this.getOpplaering(), "Maks lengde for opplæring er 1000 tegn");
    }

}
