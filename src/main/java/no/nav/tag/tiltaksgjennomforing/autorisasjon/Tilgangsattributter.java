package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;

public record Tilgangsattributter(
   String kontor,
   boolean skjermet,
   Diskresjonskode diskresjonskode
) {}
