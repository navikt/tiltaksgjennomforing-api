package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.AuditerbarAvtale;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AvtaleMinimalListevisning implements AuditerbarAvtale {
    private String id;
    private String deltakerFnr;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String bedriftNr;
    private String bedriftNavn;
    private String veilederNavIdent;
    private LocalDate startDato;
    private LocalDate sluttDato;
    private Status status;
    private Tiltakstype tiltakstype;
    private boolean erGodkjentTaushetserklæringAvMentor;
    private TilskuddPeriodeStatus gjeldendeTilskuddsperiodeStatus;
    private Instant sistEndret;

    public static AvtaleMinimalListevisning fromAvtale(Avtale avtale) {
        AvtaleMinimalListevisning avtaleMininal = AvtaleMinimalListevisning.builder()
                .id(avtale.getId().toString())
                .deltakerFnr(avtale.getDeltakerFnr() != null ? avtale.getDeltakerFnr().asString() : null)
                .deltakerEtternavn(avtale.getGjeldendeInnhold().getDeltakerEtternavn())
                .deltakerFornavn(avtale.getGjeldendeInnhold().getDeltakerFornavn())
                .bedriftNr(avtale.getBedriftNr().asString())
                .bedriftNavn(avtale.getGjeldendeInnhold().getBedriftNavn())
                .veilederNavIdent(avtale.getVeilederNavIdent() != null ? avtale.getVeilederNavIdent().asString() : null)
                .startDato(avtale.getGjeldendeInnhold().getStartDato())
                .sluttDato(avtale.getGjeldendeInnhold().getSluttDato())
                .status(avtale.statusSomEnum())
                .tiltakstype(avtale.getTiltakstype())
                .erGodkjentTaushetserklæringAvMentor(avtale.erGodkjentTaushetserklæringAvMentor())
                .gjeldendeTilskuddsperiodeStatus(avtale.getGjeldendeTilskuddsperiodestatus())
                .sistEndret(avtale.getSistEndret())
                .build();
        return avtaleMininal;
    }

    @Override
    public FnrOgBedrift getFnrOgBedrift() {
        return new FnrOgBedrift(new Fnr(getDeltakerFnr()), new BedriftNr(getBedriftNr()));
    }
}
