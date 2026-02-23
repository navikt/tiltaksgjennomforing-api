package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.KommendeOppfolgingDTO;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BegrensetAvtale(
    UUID id,
    String deltakerFornavn,
    String deltakerEtternavn,
    String bedriftNavn,
    NavIdent veilederNavIdent,
    LocalDate startDato,
    LocalDate sluttDato,
    Status status,
    Tiltakstype tiltakstype,
    KommendeOppfolgingDTO kommendeOppfolging,
    @Deprecated
    Instant oppfolgingVarselSendt,
    @Deprecated
    LocalDate kreverOppfolgingFom,
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
            avtale.getId(),
            avtale.getGjeldendeInnhold().getDeltakerFornavn(),
            avtale.getGjeldendeInnhold().getDeltakerEtternavn(),
            avtale.getGjeldendeInnhold().getBedriftNavn(),
            avtale.getVeilederNavIdent(),
            avtale.getGjeldendeInnhold().getStartDato(),
            avtale.getGjeldendeInnhold().getSluttDato(),
            avtale.getStatus(),
            avtale.getTiltakstype(),
            KommendeOppfolgingDTO.fraAvtale(avtale),
            avtale.getOppfolgingVarselSendt(),
            avtale.getKreverOppfolgingFom(),
            avtale.erGodkjentTaushetserklæringAvMentor(),
            avtale.getGjeldendeTilskuddsperiodestatus(),
            avtale.getSistEndret(),
            diskresjonskode
        );
    }
}
