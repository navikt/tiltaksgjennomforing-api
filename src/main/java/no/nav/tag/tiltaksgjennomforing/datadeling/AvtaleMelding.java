package no.nav.tag.tiltaksgjennomforing.datadeling;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Value
public class AvtaleMelding {

    private String deltakerFnr;
    private String mentorFnr;
    private String bedriftNr;
    private String veilederNavIdent;
    private Tiltakstype tiltakstype;
    private LocalDateTime opprettetTidspunkt;
    private UUID id;
    private Integer avtaleNr;
    private Instant sistEndret;
    private Instant annullertTidspunkt;
    private String annullertGrunn;
    private boolean slettemerket;
    private boolean opprettetAvArbeidsgiver;
    private String enhetGeografisk;
    private String enhetsnavnGeografisk;
    private String enhetOppfolging;
    private String enhetsnavnOppfolging;
    private boolean godkjentForEtterregistrering;
    private Kvalifiseringsgruppe kvalifiseringsgruppe;
    private Formidlingsgruppe formidlingsgruppe;
    private SortedSet<TilskuddPeriode> tilskuddPeriode = new TreeSet<>();
    private boolean feilregistrert;

    // Innhold
    private Integer versjon;
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
    private String journalpostId;
    private String arbeidsoppgaver;
    private String stillingstittel;
    private Integer stillingStyrk08;
    private Integer stillingKonseptId;
    private Integer antallDagerPerUke;
    private String refusjonKontaktpersonFornavn;
    private String refusjonKontaktpersonEtternavn;
    private String refusjonKontaktpersonTlf;

    // Mentor
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Integer mentorAntallTimer;
    private Integer mentorTimelonn;
    private String mentorTlf;

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
    private Integer sumLønnstilskuddRedusert;
    private LocalDate datoForRedusertProsent;
    private Stillingstype stillingstype;

    private List<Maal> maal = new ArrayList<>();

    // Inkluderingstilskudd
    private List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();
    private String inkluderingstilskuddBegrunnelse;

    private Integer inkluderingstilskuddTotalBeløp() {
        return inkluderingstilskuddsutgift.stream().map(inkluderingstilskuddsutgift -> inkluderingstilskuddsutgift.getBeløp())
                .collect(Collectors.toList()).stream()
                .reduce(0, Integer::sum);
    }

    // Godkjenning
    private LocalDateTime godkjentAvDeltaker;
    private LocalDateTime godkjentTaushetserklæringAvMentor;
    private LocalDateTime godkjentAvArbeidsgiver;
    private LocalDateTime godkjentAvVeileder;
    private LocalDateTime godkjentAvBeslutter;
    private LocalDateTime avtaleInngått;
    private LocalDateTime ikrafttredelsestidspunkt;
    private String godkjentAvNavIdent;
    private String godkjentAvBeslutterNavIdent;

    // Kostnadssted
    private String enhetKostnadssted;
    private String enhetsnavnKostnadssted;
    private GodkjentPaVegneGrunn godkjentPaVegneGrunn;
    private boolean godkjentPaVegneAv;
    private GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn;
    private boolean godkjentPaVegneAvArbeidsgiver;

    @Enumerated(EnumType.STRING)
    private AvtaleInnholdType innholdType;

    public static AvtaleMelding create(Avtale avtale, AvtaleInnhold avtaleInnhold) {
        return new AvtaleMelding(
                avtale.getDeltakerFnr().asString(),
                avtale.getMentorFnr().asString(),
                avtale.getBedriftNr().asString(),
                avtale.getVeilederNavIdent().asString(),
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
                avtaleInnhold.getRefusjonKontaktperson().getRefusjonKontaktpersonFornavn(),
                avtaleInnhold.getRefusjonKontaktperson().getRefusjonKontaktpersonEtternavn(),
                avtaleInnhold.getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(),
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








        );
    }
}
