package no.nav.tag.tiltaksgjennomforing.avtale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndreAvtale {

    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String deltakerTlf;
    private String bedriftNavn;
    private String arbeidsgiverFornavn;
    private String arbeidsgiverEtternavn;
    private String arbeidsgiverTlf;
    private String veilederFornavn;
    private String veilederEtternavn;
    private String veilederTlf;

    private String oppfolging;
    private String tilrettelegging;

    private LocalDate startDato;
    private LocalDate sluttDato;
    private Integer stillingprosent;
    private String arbeidsoppgaver;
    private String stillingstittel;
    private Integer stillingStyrk08;
    private Integer stillingKonseptId;

    // Arbeidstreningsfelter
    private List<Maal> maal = new ArrayList<>();

    // Lønnstilskuddsfelter
    private String arbeidsgiverKontonummer;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;
    private Boolean harFamilietilknytning;
    private String familietilknytningForklaring;
    private Double otpSats;
    private Integer otpBelop;
    private Integer arbeidsgiveravgiftBelop;
    private Integer sumLonnsutgifter;
    private Integer sumLonnstilskudd;
    private Stillingstype stillingstype;

    // Mentorfelter
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Integer mentorAntallTimer;
    private Integer mentorTimelonn;
}
