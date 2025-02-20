package no.nav.tag.tiltaksgjennomforing.autorisasjon;

public record Tilgangsattributter(
   String kontor,
   boolean skjermet,
   Diskresjonskode diskresjonskode
) {}
