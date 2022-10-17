package no.nav.tag.tiltaksgjennomforing.datadeling;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;

import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Value
public class AvtaleMelding {
    HendelseType hendelseType;
    Status avtaleStatus;

    Identifikator deltakerFnr;
    Identifikator mentorFnr;
    Identifikator bedriftNr;
    Identifikator veilederNavIdent;
    Tiltakstype tiltakstype;
    LocalDateTime opprettetTidspunkt;
    UUID id;
    Integer avtaleNr;
    Instant sistEndret;
    Instant annullertTidspunkt;
    String annullertGrunn;
    boolean slettemerket;
    boolean opprettetAvArbeidsgiver;
    String enhetGeografisk;
    String enhetsnavnGeografisk;
    String enhetOppfolging;
    String enhetsnavnOppfolging;
    boolean godkjentForEtterregistrering;
    Kvalifiseringsgruppe kvalifiseringsgruppe;
    Formidlingsgruppe formidlingsgruppe;
    SortedSet<TilskuddPeriode> tilskuddPeriode = new TreeSet<>();
    boolean feilregistrert;

    // Innhold
    Integer versjon;
    String deltakerFornavn;
    String deltakerEtternavn;
    String deltakerTlf;
    String bedriftNavn;
    String arbeidsgiverFornavn;
    String arbeidsgiverEtternavn;
    String arbeidsgiverTlf;
    String veilederFornavn;
    String veilederEtternavn;
    String veilederTlf;
    String oppfolging;
    String tilrettelegging;
    LocalDate startDato;
    LocalDate sluttDato;
    Integer stillingprosent;
    String journalpostId;
    String arbeidsoppgaver;
    String stillingstittel;
    Integer stillingStyrk08;
    Integer stillingKonseptId;
    Integer antallDagerPerUke;
    RefusjonKontaktperson refusjonKontaktperson;
    // Mentor
    String mentorFornavn;
    String mentorEtternavn;
    String mentorOppgaver;
    Integer mentorAntallTimer;
    Integer mentorTimelonn;
    String mentorTlf;

    // Lønnstilskudd
    String arbeidsgiverKontonummer;
    Integer lonnstilskuddProsent;
    Integer manedslonn;
    BigDecimal feriepengesats;
    BigDecimal arbeidsgiveravgift;
    Boolean harFamilietilknytning;
    String familietilknytningForklaring;
    Integer feriepengerBelop;
    Double otpSats;
    Integer otpBelop;
    Integer arbeidsgiveravgiftBelop;
    Integer sumLonnsutgifter;
    Integer sumLonnstilskudd;
    Integer manedslonn100pst;
    Integer sumLønnstilskuddRedusert;
    LocalDate datoForRedusertProsent;
    Stillingstype stillingstype;

    List<Maal> maal = new ArrayList<>();

    // Inkluderingstilskudd
    List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();
    String inkluderingstilskuddBegrunnelse;

    Integer inkluderingstilscskuddTotalBeløp() {
        return inkluderingstilskuddsutgift.stream().map(inkluderingstilskuddsutgift -> inkluderingstilskuddsutgift.getBeløp())
                .collect(Collectors.toList()).stream()
                .reduce(0, Integer::sum);
    }

    // Godkjenning
    LocalDateTime godkjentAvDeltaker;
    LocalDateTime godkjentTaushetserklæringAvMentor;
    LocalDateTime godkjentAvArbeidsgiver;
    LocalDateTime godkjentAvVeileder;
    LocalDateTime godkjentAvBeslutter;
    LocalDateTime avtaleInngått;
    LocalDateTime ikrafttredelsestidspunkt;
    Identifikator godkjentAvNavIdent;
    Identifikator godkjentAvBeslutterNavIdent;

    // Kostnadssted
    String enhetKostnadssted;
    String enhetsnavnKostnadssted;
    GodkjentPaVegneGrunn godkjentPaVegneGrunn;
    boolean godkjentPaVegneAv;
    GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn;
    boolean godkjentPaVegneAvArbeidsgiver;
    AvtaleInnholdType innholdType;
    Identifikator utførtAv;

    public static AvtaleMelding create(Avtale avtale, AvtaleInnhold avtaleInnhold, Identifikator utførtAv, HendelseType hendelseType) {

        return new AvtaleMelding(
                hendelseType,
                avtale.statusSomEnum(),
                avtale.getDeltakerFnr(),
                avtale.getMentorFnr(),
                avtale.getBedriftNr(),
                avtale.getVeilederNavIdent(),
                avtale.getTiltakstype(),
                avtale.getOpprettetTidspunkt(),
                avtale.getId(),
                avtale.getAvtaleNr(),
                avtale.getSistEndret(),
                avtale.getAnnullertTidspunkt(),
                avtale.getAnnullertGrunn(),
                avtale.isSlettemerket(),
                avtale.isOpprettetAvArbeidsgiver(),
                avtale.getEnhetGeografisk(),
                avtale.getEnhetsnavnGeografisk(),
                avtale.getEnhetOppfolging(),
                avtale.getEnhetsnavnOppfolging(),
                avtale.isGodkjentForEtterregistrering(),
                avtale.getKvalifiseringsgruppe(),
                avtale.getFormidlingsgruppe(),
                avtale.isFeilregistrert(),
                avtaleInnhold.getVersjon(),
                avtaleInnhold.getDeltakerFornavn(),
                avtaleInnhold.getDeltakerEtternavn(),
                avtaleInnhold.getDeltakerTlf(),
                avtaleInnhold.getBedriftNavn(),
                avtaleInnhold.getArbeidsgiverFornavn(),
                avtaleInnhold.getArbeidsgiverEtternavn(),
                avtaleInnhold.getArbeidsgiverTlf(),
                avtaleInnhold.getVeilederFornavn(),
                avtaleInnhold.getVeilederEtternavn(),
                avtaleInnhold.getVeilederTlf(),
                avtaleInnhold.getOppfolging(),
                avtaleInnhold.getTilrettelegging(),
                avtaleInnhold.getStartDato(),
                avtaleInnhold.getSluttDato(),
                avtaleInnhold.getStillingprosent(),
                avtaleInnhold.getJournalpostId(),
                avtaleInnhold.getArbeidsoppgaver(),
                avtaleInnhold.getStillingstittel(),
                avtaleInnhold.getStillingStyrk08(),
                avtaleInnhold.getStillingKonseptId(),
                avtaleInnhold.getAntallDagerPerUke(),
                avtaleInnhold.getRefusjonKontaktperson(),
                avtaleInnhold.getMentorFornavn(),
                avtaleInnhold.getMentorEtternavn(),
                avtaleInnhold.getMentorOppgaver(),
                avtaleInnhold.getMentorAntallTimer(),
                avtaleInnhold.getMentorTimelonn(),
                avtaleInnhold.getMentorTlf(),
                avtaleInnhold.getArbeidsgiverKontonummer(),
                avtaleInnhold.getLonnstilskuddProsent(),
                avtaleInnhold.getManedslonn(),
                avtaleInnhold.getFeriepengesats(),
                avtaleInnhold.getArbeidsgiveravgift(),
                avtaleInnhold.getHarFamilietilknytning(),
                avtaleInnhold.getFamilietilknytningForklaring(),
                avtaleInnhold.getFeriepengerBelop(),
                avtaleInnhold.getOtpSats(),
                avtaleInnhold.getOtpBelop(),
                avtaleInnhold.getArbeidsgiveravgiftBelop(),
                avtaleInnhold.getSumLonnsutgifter(),
                avtaleInnhold.getSumLonnstilskudd(),
                avtaleInnhold.getManedslonn100pst(),
                avtaleInnhold.getSumLønnstilskuddRedusert(),
                avtaleInnhold.getDatoForRedusertProsent(),
                avtaleInnhold.getStillingstype(),
                avtaleInnhold.getInkluderingstilskuddBegrunnelse(),
                avtaleInnhold.getGodkjentAvDeltaker(),
                avtaleInnhold.getGodkjentTaushetserklæringAvMentor(),
                avtaleInnhold.getGodkjentAvArbeidsgiver(),
                avtaleInnhold.getGodkjentAvVeileder(),
                avtaleInnhold.getGodkjentAvBeslutter(),
                avtaleInnhold.getAvtaleInngått(),
                avtaleInnhold.getIkrafttredelsestidspunkt(),
                avtaleInnhold.getGodkjentAvNavIdent(),
                avtaleInnhold.getGodkjentAvBeslutterNavIdent(),
                avtaleInnhold.getEnhetKostnadssted(),
                avtaleInnhold.getEnhetsnavnKostnadssted(),
                avtaleInnhold.getGodkjentPaVegneGrunn(),
                avtaleInnhold.isGodkjentPaVegneAv(),
                avtaleInnhold.getGodkjentPaVegneAvArbeidsgiverGrunn(),
                avtaleInnhold.isGodkjentPaVegneAvArbeidsgiver(),
                avtaleInnhold.getInnholdType(),
                utførtAv
        );
    }
}
