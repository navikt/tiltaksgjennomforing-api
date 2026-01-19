package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

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
    private BigDecimal stillingprosent;
    private String arbeidsoppgaver;
    private String stillingstittel;
    private Integer stillingStyrk08;
    private Integer stillingKonseptId;
    private BigDecimal antallDagerPerUke;

    private String refusjonKontaktpersonFornavn;
    private String refusjonKontaktpersonEtternavn;
    private String refusjonKontaktpersonTlf;
    private Boolean ønskerVarslingOmRefusjon;

    // Arbeidstreningsfelter
    private List<Maal> maal = new ArrayList<>();

    // Inkluderingstilskuddsfelter
    private List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();
    private String inkluderingstilskuddBegrunnelse;

    // Lønnstilskuddsfelter
    private String arbeidsgiverKontonummer;
    private String arbeidsgiverKid;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;
    private Double otpSats;
    private Boolean harFamilietilknytning;
    private String familietilknytningForklaring;
    private Stillingstype stillingstype;

    // Mentorfelter
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Double mentorAntallTimer;
    private String mentorTlf;
    private Integer mentorTimelonn;
    private Integer mentorValgtLonnstypeBelop;
    private MentorValgtLonnstype mentorValgtLonnstype;

    public RefusjonKontaktperson getRefusjonKontaktperson() {
        if (refusjonKontaktpersonTlf == null && refusjonKontaktpersonFornavn == null && refusjonKontaktpersonEtternavn == null) {
            return null;
        }

        return new RefusjonKontaktperson(refusjonKontaktpersonFornavn, refusjonKontaktpersonEtternavn, refusjonKontaktpersonTlf,
                ønskerVarslingOmRefusjon);
    }

    public void setRefusjonKontaktperson(RefusjonKontaktperson refusjonKontaktperson) {
        if (refusjonKontaktperson == null) {
            return;
        }
        this.refusjonKontaktpersonFornavn = refusjonKontaktperson.getRefusjonKontaktpersonFornavn();
        this.refusjonKontaktpersonEtternavn = refusjonKontaktperson.getRefusjonKontaktpersonEtternavn();
        this.refusjonKontaktpersonTlf = refusjonKontaktperson.getRefusjonKontaktpersonTlf();
        this.ønskerVarslingOmRefusjon = refusjonKontaktperson.getØnskerVarslingOmRefusjon();
    }

    public static EndreAvtale fraAvtale(Avtale avtale) {
        var innhold = avtale.getGjeldendeInnhold();
        var refusjonKontakt = ofNullable(innhold.getRefusjonKontaktperson());
        return new EndreAvtale(
                innhold.getDeltakerFornavn(),
                innhold.getDeltakerEtternavn(),
                innhold.getDeltakerTlf(),
                innhold.getBedriftNavn(),
                innhold.getArbeidsgiverFornavn(),
                innhold.getArbeidsgiverEtternavn(),
                innhold.getArbeidsgiverTlf(),
                innhold.getVeilederFornavn(),
                innhold.getVeilederEtternavn(),
                innhold.getVeilederTlf(),
                innhold.getOppfolging(),
                innhold.getTilrettelegging(),
                innhold.getStartDato(),
                innhold.getSluttDato(),
                innhold.getStillingprosent(),
                innhold.getArbeidsoppgaver(),
                innhold.getStillingstittel(),
                innhold.getStillingStyrk08(),
                innhold.getStillingKonseptId(),
                innhold.getAntallDagerPerUke(),
                refusjonKontakt.map(RefusjonKontaktperson::getRefusjonKontaktpersonFornavn).orElse(null),
                refusjonKontakt.map(RefusjonKontaktperson::getRefusjonKontaktpersonEtternavn).orElse(null),
                refusjonKontakt.map(RefusjonKontaktperson::getRefusjonKontaktpersonTlf).orElse(null),
                refusjonKontakt.map(RefusjonKontaktperson::getØnskerVarslingOmRefusjon).orElse(null),
                innhold.getMaal(),
                innhold.getInkluderingstilskuddsutgift(),
                innhold.getInkluderingstilskuddBegrunnelse(),
                innhold.getArbeidsgiverKontonummer(),
                innhold.getArbeidsgiverKid(),
                innhold.getLonnstilskuddProsent(),
                innhold.getManedslonn(),
                innhold.getFeriepengesats(),
                innhold.getArbeidsgiveravgift(),
                innhold.getOtpSats(),
                innhold.getHarFamilietilknytning(),
                innhold.getFamilietilknytningForklaring(),
                innhold.getStillingstype(),
                innhold.getMentorFornavn(),
                innhold.getMentorEtternavn(),
                innhold.getMentorOppgaver(),
                innhold.getMentorAntallTimerPerMaaned(),
                innhold.getMentorTlf(),
                innhold.getMentorTimelonn(),
                innhold.getMentorValgtLonnstypeBelop(),
                innhold.getMentorValgtLonnstype()
        );
    }
}
