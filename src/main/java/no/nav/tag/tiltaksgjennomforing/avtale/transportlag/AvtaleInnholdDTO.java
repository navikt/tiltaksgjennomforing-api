package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnholdType;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneAvArbeidsgiverGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddStrategy;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.Maal;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonKontaktperson;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Lombok
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class AvtaleInnholdDTO {
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
    private BigDecimal stillingprosent;
    private String journalpostId;
    private String arbeidsoppgaver;
    private String stillingstittel;
    private Integer stillingStyrk08;
    private Integer stillingKonseptId;
    private BigDecimal antallDagerPerUke;

    private RefusjonKontaktperson refusjonKontaktperson;

    // Mentor
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Double mentorAntallTimer;
    private Integer mentorTimelonn;
    private Integer mentorValgtLonnstypeBelop;
    private MentorValgtLonnstype mentorValgtLonnstype;
    private String mentorTlf;

    // Lønnstilskudd
    private String arbeidsgiverKontonummer;
    private String arbeidsgiverKid;
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

    // Arbeidstrening
    private List<Maal> maal = new ArrayList<>();

    // Inkluderingstilskudd
    private List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();
    private String inkluderingstilskuddBegrunnelse;

    public AvtaleInnholdDTO(AvtaleInnhold dbEntitet) {
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

    // Godkjenning
    private Instant godkjentAvDeltaker;
    private Instant godkjentTaushetserklæringAvMentor;
    private Instant godkjentAvArbeidsgiver;
    private Instant godkjentAvVeileder;
    private Instant godkjentAvBeslutter;
    private Instant avtaleInngått;
    private Instant ikrafttredelsestidspunkt;
    private NavIdent godkjentAvNavIdent;
    private NavIdent godkjentAvBeslutterNavIdent;

    // Kostnadssted
    private String enhetKostnadssted;
    private String enhetsnavnKostnadssted;

    private GodkjentPaVegneGrunn godkjentPaVegneGrunn;
    private boolean godkjentPaVegneAv;

    private GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn;
    private boolean godkjentPaVegneAvArbeidsgiver;

    private AvtaleInnholdType innholdType;
}
