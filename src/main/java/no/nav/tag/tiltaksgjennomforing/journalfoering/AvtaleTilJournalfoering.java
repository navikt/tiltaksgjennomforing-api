package no.nav.tag.tiltaksgjennomforing.journalfoering;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonKontaktperson;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
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
    private String mentorFnr;
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
    private Integer opprettetAar;
    private LocalDate sluttDato;
    private BigDecimal stillingprosent;
    private String stillingstittel;
    private Stillingstype stillingstype;
    private String arbeidsoppgaver;
    private BigDecimal antallDagerPerUke;
    private String enhetOppfolging;

    private RefusjonKontaktperson refusjonKontaktperson;

    // Lønnstilskudd
    private String arbeidsgiverKontonummer;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;
    private Boolean harFamilietilknytning;
    private String familietilknytningForklaring;
    private Integer feriepengerBelop;
    private Double otpSats;
    private Integer otpBelop;
    private Integer arbeidsgiveravgiftBelop;
    private Integer sumLonnsutgifter;
    private Integer sumLonnstilskudd;
    private Integer manedslonn100pst;

    // Inkluderingstilskudd
    private List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();
    private String inkluderingstilskuddBegrunnelse;

    // mentor
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Double mentorAntallTimer;
    private Integer mentorTimelonn;

    private List<MaalTilJournalfoering> maal = new ArrayList<>();
    private List<OppgaveTilJournalFoering> oppgaver = new ArrayList<>();

    private GodkjentPaVegneGrunnTilJournalfoering godkjentPaVegneGrunn;

    private LocalDate godkjentAvDeltaker;
    private LocalDate godkjentAvArbeidsgiver;
    private LocalDate godkjentAvVeileder;
    private LocalDate godkjentTaushetserklæringAvMentor;
    private List<TilskuddPeriode> tilskuddsPerioder;
    private boolean godkjentPaVegneAv;
    private Avtalerolle avtalerolle;

}
