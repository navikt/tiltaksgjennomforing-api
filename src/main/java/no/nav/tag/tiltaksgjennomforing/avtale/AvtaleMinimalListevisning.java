package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AvtaleMinimalListevisning {
    private String id;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String bedriftNavn;
    private String veilederNavIdent;
    private LocalDate startDato;
    private LocalDate sluttDato;
    private Status status;
    private Tiltakstype tiltakstype;
    private LocalDate kreverOppfolgingFom;
    private boolean erGodkjentTaushetserklæringAvMentor;
    private TilskuddPeriodeStatus gjeldendeTilskuddsperiodeStatus;
    private Instant sistEndret;

    public static AvtaleMinimalListevisning fromAvtale(Avtale avtale) {
        return AvtaleMinimalListevisning.builder()
                .id(avtale.getId().toString())
                .deltakerEtternavn(avtale.getGjeldendeInnhold().getDeltakerEtternavn())
                .deltakerFornavn(avtale.getGjeldendeInnhold().getDeltakerFornavn())
                .bedriftNavn(avtale.getGjeldendeInnhold().getBedriftNavn())
                .veilederNavIdent(avtale.getVeilederNavIdent() != null ? avtale.getVeilederNavIdent().asString() : null)
                .startDato(avtale.getGjeldendeInnhold().getStartDato())
                .sluttDato(avtale.getGjeldendeInnhold().getSluttDato())
                .status(avtale.getStatus())
                .tiltakstype(avtale.getTiltakstype())
                .kreverOppfolgingFom(avtale.getKreverOppfolgingFom())
                .erGodkjentTaushetserklæringAvMentor(avtale.erGodkjentTaushetserklæringAvMentor())
                .gjeldendeTilskuddsperiodeStatus(avtale.getGjeldendeTilskuddsperiodestatus())
                .sistEndret(avtale.getSistEndret())
                .build();
    }
}
