package no.nav.tag.tiltaksgjennomforing.refusjon;


import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@FieldNameConstants
public class Refusjonsmelding {

  private String id;
  private String deltaker;
  private Tiltakstype tiltakstype;
  private Status status;
  private String deltakerFnr;
  private String veileder;
  private String bedrift;
  private String bedriftnummer;
  private int feriedager;
  private int trekkFeriedagerBeløp;
  private int sykedager;
  private int sykepenger;
  private int stillingsprosent;
  private int månedslønn;
  private int nettoMånedslønn;
  private double satsOtp;
  private int beløpOtp;
  private double satsFeriepenger;
  private int feriepenger;
  private double satsArbeidsgiveravgift;
  private int arbeidsgiveravgift;
  private int sumUtgifterArbeidsgiver;
  private double satsRefusjon;
  private int refusjonPrMåned;
  private LocalDate fraDato;
  private LocalDate tilDato;
  private LocalDateTime opprettetTidspunkt;
}
