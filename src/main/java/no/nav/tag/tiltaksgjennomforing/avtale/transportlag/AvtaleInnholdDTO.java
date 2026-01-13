package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnholdType;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneAvArbeidsgiverGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddStrategy;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonKontaktperson;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Lombok
@Builder(toBuilder = true)
public record AvtaleInnholdDTO(
    UUID id,
    Integer versjon,

    String deltakerFornavn,
    String deltakerEtternavn,
    String deltakerTlf,
    String bedriftNavn,
    String arbeidsgiverFornavn,
    String arbeidsgiverEtternavn,
    String arbeidsgiverTlf,
    String veilederFornavn,
    String veilederEtternavn,
    String veilederTlf,
    String oppfolging,
    String tilrettelegging,
    LocalDate startDato,
    LocalDate sluttDato,
    BigDecimal stillingprosent,
    String journalpostId,
    String arbeidsoppgaver,
    String stillingstittel,
    Integer stillingStyrk08,
    Integer stillingKonseptId,
    BigDecimal antallDagerPerUke,

    RefusjonKontaktperson refusjonKontaktperson,

    // Mentor
    String mentorFornavn,
    String mentorEtternavn,
    String mentorOppgaver,
    Double mentorAntallTimer,
    Integer mentorTimelonn,
    Integer mentorValgtLonnstypeBelop,
    MentorValgtLonnstype mentorValgtLonnstype,
    String mentorTlf,

    // Lønnstilskudd
    String arbeidsgiverKontonummer,
    String arbeidsgiverKid,
    Integer lonnstilskuddProsent,
    Integer manedslonn,
    BigDecimal feriepengesats,
    BigDecimal arbeidsgiveravgift,
    Boolean harFamilietilknytning,
    String familietilknytningForklaring,
    Integer feriepengerBelop,
    Double otpSats,
    Integer otpBelop,
    Integer arbeidsgiveravgiftBelop,
    Integer sumLonnsutgifter,
    Integer sumLonnstilskudd,
    Integer manedslonn100pst,
    Integer sumLønnstilskuddRedusert,
    LocalDate datoForRedusertProsent,
    Stillingstype stillingstype,

    // Arbeidstrening
    List<MaalDTO> maal,

    // Inkluderingstilskudd
    List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift,
    String inkluderingstilskuddBegrunnelse,
    Instant godkjentAvDeltaker,
    Instant godkjentTaushetserklæringAvMentor,
    Instant godkjentAvArbeidsgiver,
    Instant godkjentAvVeileder,
    Instant godkjentAvBeslutter,
    Instant avtaleInngått,
    Instant ikrafttredelsestidspunkt,
    NavIdent godkjentAvNavIdent,
    NavIdent godkjentAvBeslutterNavIdent,

    // Kostnadssted
    String enhetKostnadssted,
    String enhetsnavnKostnadssted,

    GodkjentPaVegneGrunn godkjentPaVegneGrunn,
    boolean godkjentPaVegneAv,

    GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn,
    boolean godkjentPaVegneAvArbeidsgiver,

    AvtaleInnholdType innholdType
) {

    public AvtaleInnholdDTO(AvtaleInnhold dbEntitet) {
        this(
            dbEntitet.getId(),
            dbEntitet.getVersjon(),
            dbEntitet.getDeltakerFornavn(),
            dbEntitet.getDeltakerEtternavn(),
            dbEntitet.getDeltakerTlf(),
            dbEntitet.getBedriftNavn(),
            dbEntitet.getArbeidsgiverFornavn(),
            dbEntitet.getArbeidsgiverEtternavn(),
            dbEntitet.getArbeidsgiverTlf(),
            dbEntitet.getVeilederFornavn(),
            dbEntitet.getVeilederEtternavn(),
            dbEntitet.getVeilederTlf(),
            dbEntitet.getOppfolging(),
            dbEntitet.getTilrettelegging(),
            dbEntitet.getStartDato(),
            dbEntitet.getSluttDato(),
            dbEntitet.getStillingprosent(),
            dbEntitet.getJournalpostId(),
            dbEntitet.getArbeidsoppgaver(),
            dbEntitet.getStillingstittel(),
            dbEntitet.getStillingStyrk08(),
            dbEntitet.getStillingKonseptId(),
            dbEntitet.getAntallDagerPerUke(),
            dbEntitet.getRefusjonKontaktperson(),
            dbEntitet.getMentorFornavn(),
            dbEntitet.getMentorEtternavn(),
            dbEntitet.getMentorOppgaver(),
            dbEntitet.getMentorAntallTimer(),
            dbEntitet.getMentorTimelonn(),
            dbEntitet.getMentorValgtLonnstypeBelop(),
            dbEntitet.getMentorValgtLonnstype(),
            dbEntitet.getMentorTlf(),
            dbEntitet.getArbeidsgiverKontonummer(),
            dbEntitet.getArbeidsgiverKid(),
            dbEntitet.getLonnstilskuddProsent(),
            dbEntitet.getManedslonn(),
            dbEntitet.getFeriepengesats(),
            dbEntitet.getArbeidsgiveravgift(),
            dbEntitet.getHarFamilietilknytning(),
            dbEntitet.getFamilietilknytningForklaring(),
            dbEntitet.getFeriepengerBelop(),
            dbEntitet.getOtpSats(),
            dbEntitet.getOtpBelop(),
            dbEntitet.getArbeidsgiveravgiftBelop(),
            dbEntitet.getSumLonnsutgifter(),
            dbEntitet.getSumLonnstilskudd(),
            dbEntitet.getManedslonn100pst(),
            dbEntitet.getSumLønnstilskuddRedusert(),
            dbEntitet.getDatoForRedusertProsent(),
            dbEntitet.getStillingstype(),
            dbEntitet.getMaal().stream().map(MaalDTO::new).toList(),
            dbEntitet.getInkluderingstilskuddsutgift(),
            dbEntitet.getInkluderingstilskuddBegrunnelse(),
            dbEntitet.getGodkjentAvDeltaker(),
            dbEntitet.getGodkjentTaushetserklæringAvMentor(),
            dbEntitet.getGodkjentAvArbeidsgiver(),
            dbEntitet.getGodkjentAvVeileder(),
            dbEntitet.getGodkjentAvBeslutter(),
            dbEntitet.getAvtaleInngått(),
            dbEntitet.getIkrafttredelsestidspunkt(),
            dbEntitet.getGodkjentAvNavIdent(),
            dbEntitet.getGodkjentAvBeslutterNavIdent(),
            dbEntitet.getEnhetKostnadssted(),
            dbEntitet.getEnhetsnavnKostnadssted(),
            dbEntitet.getGodkjentPaVegneGrunn(),
            dbEntitet.isGodkjentPaVegneAv(),
            dbEntitet.getGodkjentPaVegneAvArbeidsgiverGrunn(),
            dbEntitet.isGodkjentPaVegneAvArbeidsgiver(),
            dbEntitet.getInnholdType()
        );
    }

    @JsonProperty
    public Integer inkluderingstilskuddTotalBeløp() {
        return inkluderingstilskuddsutgift.stream()
            .map(Inkluderingstilskuddsutgift::getBeløp)
            .reduce(0, Integer::sum);
    }

    @JsonProperty
    public Integer inkluderingstilskuddSats() {
        return InkluderingstilskuddStrategy.getInkluderingstilskuddSats(this.sluttDato);
    }
}
