package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BegrensetBeslutterAvtale(
    UUID id,
    Integer avtaleNr,
    Tiltakstype tiltakstype,
    NavIdent veilederNavIdent,
    String deltakerFornavn,
    String deltakerEtternavn,
    String bedriftNavn,
    LocalDate startDato,
    LocalDate sluttDato,
    String status,
    String antallUbehandlet,
    LocalDateTime opprettetTidspunkt,
    Instant sistEndret,
    boolean harReturnertSomKanBehandles,
    Diskresjonskode diskresjonskode
) {
    public static BegrensetBeslutterAvtale fraEntitetMedDiskresjonskode(BeslutterOversiktEntity entitet, Diskresjonskode diskresjonskode) {
        return new BegrensetBeslutterAvtale(
            entitet.getId(),
            entitet.getAvtaleNr(),
            entitet.getTiltakstype(),
            entitet.getVeilederNavIdent(),
            entitet.getDeltakerFornavn(),
            entitet.getDeltakerEtternavn(),
            entitet.getBedriftNavn(),
            entitet.getStartDato(),
            entitet.getSluttDato(),
            entitet.getStatus(),
            entitet.getAntallUbehandlet(),
            entitet.getOpprettetTidspunkt(),
            entitet.getSistEndret(),
            entitet.getHarReturnertSomKanBehandles() == Boolean.TRUE,
            diskresjonskode
        );
    }
}
