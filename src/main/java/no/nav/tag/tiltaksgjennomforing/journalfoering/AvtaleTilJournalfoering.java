package no.nav.tag.tiltaksgjennomforing.journalfoering;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvtaleTilJournalfoering {
    private Tiltakstype tiltakstype;
    private UUID avtaleId;
    private UUID avtaleVersjonId;
    private LocalDate opprettet;

    private String deltakerFnr;
    private String bedriftNr;
    private String veilederNavIdent;

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
    private Integer versjon;

    private String oppfolging;
    private String tilrettelegging;

    private LocalDate startDato;
    private LocalDate sluttDato;
    private Integer stillingprosent;
    private String stillingstittel;
    private String arbeidsoppgaver;

    // Lønnstilskudd
    private String arbeidsgiverKontonummer;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;
    private Boolean harFamilietilknytning;
    private String familietilknytningForklaring;
    private Integer feriepengerBelop;
    private Integer otpBelop;
    private Integer arbeidsgiveravgiftBelop;
    private Integer sumLonntilskudd;
    private Integer utbetaltLonntilskudd;

    // mentor
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Integer mentorAntallTimer;
    private Integer mentorTimelonn;

    private List<MaalTilJournalfoering> maal = new ArrayList<>();
    private List<OppgaveTilJournalFoering> oppgaver = new ArrayList<>();

    private GodkjentPaVegneGrunnTilJournalfoering godkjentPaVegneGrunn;

    private LocalDate godkjentAvDeltaker;
    private LocalDate godkjentAvArbeidsgiver;
    private LocalDate godkjentAvVeileder;
    private boolean godkjentPaVegneAv;
}