package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
