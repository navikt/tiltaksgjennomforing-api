package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.Maal;
import no.nav.tag.tiltaksgjennomforing.avtale.MaalKategori;

import java.util.List;
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

    public static List<MaalDTO> map(AvtaleInnhold dbEntity) {
        return map(dbEntity.getMaal());
    }

    public static List<MaalDTO> map(List<Maal> maal) {
        return maal.stream().map(MaalDTO::new).toList();
    }
}
