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
public class IkkeLonnstilskuddAvtaleBeregningStrategy extends BeregningStrategy {
    public IkkeLonnstilskuddAvtaleBeregningStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder() { return Collections.emptyList(); }

    @Override
    public void endreBeregning(EndreTilskuddsberegning endreTilskuddsberegning) {}

    @Override
    public void reberegnTotal() {}

    @Override
    public void forleng(LocalDate gammelSluttDato, LocalDate nySluttDato){}

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(
        LocalDate startDato,
        LocalDate sluttDato
    ) {
        log.error("Uventet feil i utregning av tilskuddsperioder med startdato: {}, sluttdato: {} avtaleId: {}", startDato, sluttDato, avtale.getId());
        throw new FeilkodeException(Feilkode.FORLENG_MIDLERTIDIG_IKKE_TILGJENGELIG);
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp() {
        return true;
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder() {
        return false;
    }

}
