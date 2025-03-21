package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "avtale_forkortet")
public class AvtaleForkortetEntitet {
    @Id
    private UUID id;
    private UUID avtaleId;
    private UUID avtaleInnholdId;
    private Instant tidspunkt;
    @Convert(converter = IdentifikatorConverter.class)
    private Identifikator utførtAv;
    private LocalDate nySluttDato;
    private String grunn;
    private String annetGrunn;

    public AvtaleForkortetEntitet() {
    }

    public AvtaleForkortetEntitet(Avtale avtale, AvtaleInnhold avtaleInnhold, Identifikator utførtAv, LocalDate nySluttDato, String grunn, String annetGrunn) {
        this.id = UUID.randomUUID();
        this.avtaleId = avtale.getId();
        this.avtaleInnholdId = avtaleInnhold.getId();
        this.tidspunkt = Now.instant();
        this.utførtAv = utførtAv;
        this.nySluttDato = nySluttDato;
        this.grunn = grunn;
        this.annetGrunn = annetGrunn;
    }


}
