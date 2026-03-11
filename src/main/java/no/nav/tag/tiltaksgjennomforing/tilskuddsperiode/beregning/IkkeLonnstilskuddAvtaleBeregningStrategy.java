package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
public class IkkeLonnstilskuddAvtaleBeregningStrategy implements BeregningStrategy {

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) { return Collections.emptyList(); }

    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {}

    @Override
    public void reberegnTotal(Avtale avtale) {}

    @Override
    public Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        return 0;
    }

    @Override
    public void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato){}

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(
        Avtale avtale,
        LocalDate startDato,
        LocalDate sluttDato
    ) {
        log.error("Uventet feil i utregning av tilskuddsperioder med startdato: {}, sluttdato: {} avtaleId: {}", startDato, sluttDato, avtale.getId());
        throw new FeilkodeException(Feilkode.FORLENG_MIDLERTIDIG_IKKE_TILGJENGELIG);
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(Avtale avtale) {
        return true;
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(Avtale avtale) {
        return false;
    }

}
