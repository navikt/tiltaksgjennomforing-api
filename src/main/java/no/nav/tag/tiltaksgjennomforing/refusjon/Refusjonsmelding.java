package no.nav.tag.tiltaksgjennomforing.refusjon;


import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Refusjonsmelding {
  private String tilskuddPeriodeId;
  private String avtaleInnholdId;;
  private String tiltakstype;
  private String deltakerFnr;
  private String veilederNavIdent;
  private String bedrift;
  private String bedriftnummer;
  private Integer tilskuddBeløp;
  private LocalDate tilskuddFraDato;
  private LocalDate tilskuddTilDato;
  private LocalDateTime opprettetTidspunkt;


  public static Refusjonsmelding fraAvtale(Avtale avtale){
    //TDO: finn en bedre måte å hente gjeldene tilskudd periode
    TilskuddPeriode tilskuddPeriode = avtale.gjeldendeInnhold().getTilskuddPeriode().stream().findFirst().orElseThrow( () -> new RefusjonFeilException("Tilskuddperiode er tom"));
    return new Refusjonsmelding(tilskuddPeriode.getId().toString(),
        avtale.getAvtaleInnholdId().toString(),
        avtale.getTiltakstype().toString(),
        avtale.getDeltakerFnr().asString(),
        avtale.getVeilederNavIdent().asString(),
        avtale.getBedriftNavn(),
        avtale.getBedriftNr().asString(),
        tilskuddPeriode.getBeløp(),
        tilskuddPeriode.getStartDato(),
        tilskuddPeriode.getSluttDato(),
        LocalDateTime.now()
        );
  }


}
