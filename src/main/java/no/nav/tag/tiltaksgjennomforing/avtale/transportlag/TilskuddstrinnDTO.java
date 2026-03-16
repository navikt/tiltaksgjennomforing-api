package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.BeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.GenerellLonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.Tilskuddstrinn;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record TilskuddstrinnDTO(
    LocalDate start,
    LocalDate slutt,
    Integer prosent,
    Integer belopPerMnd
) {
    public TilskuddstrinnDTO(Tilskuddstrinn tilskuddstrinn) {
        this(
            tilskuddstrinn.start(),
            tilskuddstrinn.slutt(),
            tilskuddstrinn.prosent(),
            tilskuddstrinn.belopPerMnd()
        );
    }

    public static List<TilskuddstrinnDTO> map(AvtaleInnhold db) {
        Avtale avtale = db.getAvtale();
        BeregningStrategy strategy = avtale.hentBeregningStrategi();
        if (strategy instanceof GenerellLonnstilskuddAvtaleBeregningStrategy ltsStrategy) {
            return ltsStrategy.getTilskuddstrinn(avtale).stream().map(TilskuddstrinnDTO::new).toList();
        }
        return Collections.emptyList();
    }
}
