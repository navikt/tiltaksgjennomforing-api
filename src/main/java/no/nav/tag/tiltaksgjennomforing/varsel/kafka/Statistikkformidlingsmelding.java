package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistikkformidlingsmelding {

  private String organisasjonsnummer;
  private Stillingstype stillingstype;
  private String yrke;
  private int andelLonnstilskudd;
  private Tiltakstype tiltakstype;
  private String avtaleId;
  private String navn;

  public static Statistikkformidlingsmelding fraAvtale(Avtale avtale) {
    return new Statistikkformidlingsmelding(
        avtale.getBedriftNr().toString(), avtale.getStillingstype(),
        avtale.getStillingstittel(), avtale.getLonnstilskuddProsent(),
        avtale.getTiltakstype(), avtale.getId().toString(),
        String.format("%s %s", avtale.getDeltakerFornavn(), avtale.getDeltakerEtternavn()));
  }
}
