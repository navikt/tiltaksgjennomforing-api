package no.nav.tag.tiltaksgjennomforing.avtale;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlleredeRegistrertAvtale {
    private static final List<Tiltakstype> TILTAK_SOM_KAN_KOMBINERES = List.of(
        Tiltakstype.INKLUDERINGSTILSKUDD,
        Tiltakstype.MENTOR
    );
    private static final List<Tiltakstype> TILTAK_SOM_IKKE_KAN_KOMBINERES = List.of(
        Tiltakstype.SOMMERJOBB,
        Tiltakstype.ARBEIDSTRENING,
        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
        Tiltakstype.VARIG_LONNSTILSKUDD,
        Tiltakstype.VTAO
    );

    private UUID id;
    private Integer avtaleNr;
    private Tiltakstype tiltakstype;
    private Fnr deltakerFnr;
    private BedriftNr bedriftNr;
    private NavIdent veilederNavIdent;
    private Status status;
    private Avtaleopphav opphav;

    private LocalDate startDato;
    private LocalDate sluttDato;
    private Instant godkjentAvVeileder;
    private Instant godkjentAvBeslutter;
    private Instant avtaleInngått;

    private static List<AlleredeRegistrertAvtale> filtrerAvtaler(Stream<Avtale> avtaler) {
        return avtaler.map(AlleredeRegistrertAvtale::setAvtaleFelter).toList();
    }

    public static AlleredeRegistrertAvtale setAvtaleFelter(Avtale avtale) {
        AlleredeRegistrertAvtale alleredeRegistrertAvtale = new AlleredeRegistrertAvtale();
        alleredeRegistrertAvtale.setId(avtale.getId());
        alleredeRegistrertAvtale.setAvtaleNr(avtale.getAvtaleNr());
        alleredeRegistrertAvtale.setTiltakstype(avtale.getTiltakstype());
        alleredeRegistrertAvtale.setDeltakerFnr(avtale.getDeltakerFnr());
        alleredeRegistrertAvtale.setBedriftNr(avtale.getBedriftNr());
        alleredeRegistrertAvtale.setStatus(avtale.getStatus());
        alleredeRegistrertAvtale.setVeilederNavIdent(avtale.getVeilederNavIdent());
        alleredeRegistrertAvtale.setOpphav(avtale.getOpphav());
        alleredeRegistrertAvtale.setStartDato(avtale.getGjeldendeInnhold().getStartDato());
        alleredeRegistrertAvtale.setSluttDato(avtale.getGjeldendeInnhold().getSluttDato());
        alleredeRegistrertAvtale.setGodkjentAvVeileder(avtale.getGjeldendeInnhold().getGodkjentAvVeileder());
        alleredeRegistrertAvtale.setGodkjentAvBeslutter(avtale.getGjeldendeInnhold().getGodkjentAvBeslutter());
        alleredeRegistrertAvtale.setAvtaleInngått(avtale.getGjeldendeInnhold().getAvtaleInngått());
        return alleredeRegistrertAvtale;
    }

    public static List<AlleredeRegistrertAvtale> filtrerAvtaleDeltakerAlleredeErRegistrertPaa(
            List<Avtale> alleAvtalerPaaDeltaker,
            Tiltakstype tiltakstype
    ) {
        if (TILTAK_SOM_KAN_KOMBINERES.contains(tiltakstype)) {
            return filtrerAvtaler(alleAvtalerPaaDeltaker.stream()
                .filter(avtale -> avtale.getTiltakstype().equals(tiltakstype)));
        }
        return filtrerAvtaler(alleAvtalerPaaDeltaker.stream()
            .filter(avtale -> TILTAK_SOM_IKKE_KAN_KOMBINERES.contains(avtale.getTiltakstype())));
    }
}
