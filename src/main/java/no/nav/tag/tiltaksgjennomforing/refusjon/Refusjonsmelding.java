package no.nav.tag.tiltaksgjennomforing.refusjon;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

@Value
public class Refusjonsmelding {
  UUID tilskuddPeriodeId;
  UUID avtaleInnholdId;
  Tiltakstype tiltakstype;
  String deltakerFornavn;
  String deltakerEtternavn;
  Identifikator deltakerFnr;
  NavIdent veilederNavIdent;
  String bedriftNavn;
  BedriftNr bedriftnummer;
  Integer tilskuddBeløp;
  LocalDate tilskuddFraDato;
  LocalDate tilskuddTilDato;
  LocalDateTime opprettetTidspunkt;

  public static Refusjonsmelding fraAvtale(Avtale avtale){
    //TODO: finn en bedre måte å hente gjeldende tilskudd periode som blir sendt
    TilskuddPeriode gjeldendeTilskuddPeriode = avtale.gjeldendeInnhold()
        .getTilskuddPeriode()
        .stream()
        .min(Comparator.comparing(TilskuddPeriode::getStartDato))
        .orElseThrow(() -> new RefusjonFeilException("Fant ikke TilskuddPeriode."));

    return new Refusjonsmelding(gjeldendeTilskuddPeriode.getId(),
        avtale.getAvtaleInnholdId(),
        avtale.getTiltakstype(),
        avtale.getDeltakerFornavn(),
        avtale.getDeltakerEtternavn(),
        avtale.getDeltakerFnr(),
        avtale.getVeilederNavIdent(),
        avtale.getBedriftNavn(),
        avtale.getBedriftNr(),
        gjeldendeTilskuddPeriode.getBeløp(),
        gjeldendeTilskuddPeriode.getStartDato(),
        gjeldendeTilskuddPeriode.getSluttDato(),
        LocalDateTime.now()
        );
  }


}
