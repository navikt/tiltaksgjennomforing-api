package no.nav.tag.tiltaksgjennomforing.datadeling;

import no.nav.tag.tiltaksgjennomforing.avtale.Maal;
import no.nav.tag.tiltaksgjennomforing.avtale.MaalKategori;

import java.util.List;
import java.util.UUID;

public record AvtaleMeldingMaal(
    UUID id,
    MaalKategori kategori,
    String beskrivelse
) {
    public static List<AvtaleMeldingMaal> map(List<Maal> maal) {
        return maal.stream()
            .map(m -> new AvtaleMeldingMaal(m.getId(), m.getKategori(), m.getBeskrivelse()))
            .toList();
    }
}
