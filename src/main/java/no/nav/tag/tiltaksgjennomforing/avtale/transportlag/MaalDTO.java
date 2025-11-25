package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.MaalKategori;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class MaalDTO {
    private UUID id = UUID.randomUUID();
    private MaalKategori kategori;
    private String beskrivelse;

    public MaalDTO() {}
}
