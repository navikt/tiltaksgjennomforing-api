package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.Maal;
import no.nav.tag.tiltaksgjennomforing.avtale.MaalKategori;

import java.util.UUID;

@FieldNameConstants
public record MaalDTO(
    UUID id,
    MaalKategori kategori,
    String beskrivelse) {

    public MaalDTO(Maal dbEntitet) {
        this(
            dbEntitet.getId(),
            dbEntitet.getKategori(),
            dbEntitet.getBeskrivelse()
        );
    }
}
