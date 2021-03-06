package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

@Value
public class Statistikkformidlingsmelding {

  String organisasjonsnummer;
  Stillingstype stillingstype;
  String yrke;
  Integer andelLonnstilskudd;
  Tiltakstype tiltakstype;
  String avtaleId;
  String navn;
  String avtaleInnholdId;

  public static Statistikkformidlingsmelding fraAvtale(Avtale avtale) {
    return new Statistikkformidlingsmelding(
        avtale.getBedriftNr().toString(), avtale.getStillingstype(),
        avtale.getStillingstittel(), avtale.getLonnstilskuddProsent(),
        avtale.getTiltakstype(), avtale.getId().toString(),
        String.format("%s %s", avtale.getDeltakerFornavn(), avtale.getDeltakerEtternavn()),
        avtale.getAvtaleInnholdId().toString());
  }
}
