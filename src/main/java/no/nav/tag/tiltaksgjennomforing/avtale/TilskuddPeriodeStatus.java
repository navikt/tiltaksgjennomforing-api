package no.nav.tag.tiltaksgjennomforing.avtale;

public enum TilskuddPeriodeStatus {
  UBEHANDLET("Ubehandlet"), GODKJENT("Godkjent"), AVSLÅTT("Avslått"), UKJENT("Ukjent");

  private String verdi;

  TilskuddPeriodeStatus(String verdi) {
    this.verdi = verdi;
  }

  public String value() {
    return this.verdi;
  }
}
