package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
public class IkkeLonnstilskuddAvtaleBeregningStrategy implements LonnstilskuddAvtaleBeregningStrategy {

    @Override
    public void genererNyeTilskuddsperioder(Avtale avtale) {}

    @Override
    public List<TilskuddPeriode> hentTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {return List.of(); }

    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {}

    @Override
    public void reberegnTotal(Avtale avtale) {}
    public Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        return 0;
    }

    public void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato){}

    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(UUID id, Tiltakstype tiltakstype, Integer sumLønnstilskuddPerMåned, LocalDate datoFraOgMed, LocalDate datoTilOgMed, Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMånedRedusert) {
        log.error("Uventet feil i utregning av tilskuddsperioder med startdato: {}, sluttdato: {}, datoForRedusertProsent: {}, avtaleId: {}", datoFraOgMed, datoTilOgMed, datoForRedusertProsent, id);
        throw new FeilkodeException(Feilkode.FORLENG_MIDLERTIDIG_IKKE_TILGJENGELIG);
    }
}
