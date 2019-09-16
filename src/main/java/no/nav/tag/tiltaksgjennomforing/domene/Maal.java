package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Maal {
    @Id
    private UUID id;
    private LocalDateTime opprettetTidspunkt;
    private String kategori;
    private String beskrivelse;

    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (opprettetTidspunkt == null) {
            opprettetTidspunkt = LocalDateTime.now();
        }
    }
    public void settNewIdOgOpprettetTidspunkt() {
        id=UUID.randomUUID();
        setOpprettetTidspunkt(LocalDateTime.now());
    }


    public void sjekkMaalLengde() {
        Utils.sjekkAtTekstIkkeOverskrider1000Tegn(this.getBeskrivelse(), "Maks lengde for mål er 1000 tegn");
    }

}
