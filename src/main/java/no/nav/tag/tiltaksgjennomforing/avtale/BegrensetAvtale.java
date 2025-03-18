package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;

import java.time.Instant;
import java.time.LocalDate;

public record BegrensetAvtale(
    String id,
    String deltakerFornavn,
    String deltakerEtternavn,
    String bedriftNavn,
    String veilederNavIdent,
    LocalDate startDato,
    LocalDate sluttDato,
    Status status,
    Tiltakstype tiltakstype,
    Instant oppfolgingVarselSendt,
    boolean erGodkjentTaushetserklæringAvMentor,
    TilskuddPeriodeStatus gjeldendeTilskuddsperiodeStatus,
    Instant sistEndret,
    Diskresjonskode diskresjonskode
) {
    public static BegrensetAvtale fraAvtale(Avtale avtale) {
        return fraAvtaleMedDiskresjonskode(avtale, null);
    }

    public static BegrensetAvtale fraAvtaleMedDiskresjonskode(Avtale avtale, Diskresjonskode diskresjonskode) {
        return new BegrensetAvtale(
            avtale.getId().toString(),
            avtale.getGjeldendeInnhold().getDeltakerFornavn(),
            avtale.getGjeldendeInnhold().getDeltakerEtternavn(),
            avtale.getGjeldendeInnhold().getBedriftNavn(),
            avtale.getVeilederNavIdent() != null ? avtale.getVeilederNavIdent().asString() : null,
            avtale.getGjeldendeInnhold().getStartDato(),
            avtale.getGjeldendeInnhold().getSluttDato(),
            avtale.getStatus(),
            avtale.getTiltakstype(),
            avtale.getOppfolgingVarselSendt(),
            avtale.erGodkjentTaushetserklæringAvMentor(),
            avtale.getGjeldendeTilskuddsperiodestatus(),
            avtale.getSistEndret(),
            diskresjonskode
        );
    }
}
