package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.MaalDTO;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
public class Maal {
    @Id
    private UUID id = UUID.randomUUID();
    @Enumerated(EnumType.STRING)
    private MaalKategori kategori;
    private String beskrivelse;
    @ManyToOne
    @JoinColumn(name = "avtale_innhold")
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    public Maal() {}

    public Maal(Maal fra) {
        kategori = fra.kategori;
        beskrivelse = fra.beskrivelse;
    }

    public Maal(MaalDTO dto) {
        id = dto.id() != null ? dto.id() : UUID.randomUUID();
        kategori = dto.kategori();
        beskrivelse = dto.beskrivelse();
    }

    public static List<Maal> map(List<MaalDTO> dto) {
        return dto.stream().map(Maal::new).toList();
    }
}
