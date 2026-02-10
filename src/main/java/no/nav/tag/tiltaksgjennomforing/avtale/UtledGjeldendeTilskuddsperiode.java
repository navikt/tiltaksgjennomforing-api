package no.nav.tag.tiltaksgjennomforing.avtale;

public record UtledGjeldendeTilskuddsperiode(
    TilskuddPeriode tilskuddPeriode,
    UtledGjeldendeTilskuddsperiode.Forklaring forklaring
) {
    public enum Forklaring {
        INGEN_AKTIVE_TILSKUDDSPERIODER,
        FORSTE_AVSLATTE_TILSKUDDSPERIODE,
        FORSTE_TILSKUDDSPERIODE_SOM_KAN_BEHANDLES,
        SISTE_GODKJENTE_TILSKUDDSPERIODE,
        FORSTE_AKTIVE_TILSKUDDSPERIODE,
    }

    public static UtledGjeldendeTilskuddsperiode empty(UtledGjeldendeTilskuddsperiode.Forklaring forklaring) {
        return new UtledGjeldendeTilskuddsperiode(null, forklaring);
    }
}
