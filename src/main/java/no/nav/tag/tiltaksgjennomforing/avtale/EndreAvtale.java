package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private Integer antallDagerPerUke;

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

    private VtaoFelter vtao;

    public EndreAvtale(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();

        this.deltakerFornavn = avtaleInnhold.getDeltakerFornavn();
        this.deltakerEtternavn = avtaleInnhold.getDeltakerEtternavn();
        this.deltakerTlf = avtaleInnhold.getDeltakerTlf();
        this.bedriftNavn = avtaleInnhold.getBedriftNavn();
        this.arbeidsgiverFornavn = avtaleInnhold.getArbeidsgiverFornavn();
        this.arbeidsgiverEtternavn = avtaleInnhold.getArbeidsgiverEtternavn();
        this.arbeidsgiverTlf = avtaleInnhold.getArbeidsgiverTlf();
        this.veilederFornavn = avtaleInnhold.getVeilederFornavn();
        this.veilederEtternavn = avtaleInnhold.getVeilederEtternavn();
        this.veilederTlf = avtaleInnhold.getVeilederTlf();
        this.oppfolging = avtaleInnhold.getOppfolging();
        this.tilrettelegging = avtaleInnhold.getTilrettelegging();

        this.startDato = avtaleInnhold.getStartDato();
        this.sluttDato = avtaleInnhold.getSluttDato();
        this.stillingprosent = avtaleInnhold.getStillingprosent();
        this.arbeidsoppgaver = avtaleInnhold.getArbeidsoppgaver();
        this.stillingstittel = avtaleInnhold.getStillingstittel();
        this.stillingStyrk08 = avtaleInnhold.getStillingStyrk08();
        this.stillingKonseptId = avtaleInnhold.getStillingKonseptId();
        this.antallDagerPerUke = avtaleInnhold.getAntallDagerPerUke();

        this.refusjonKontaktpersonFornavn = avtaleInnhold.getRefusjonKontaktperson().getRefusjonKontaktpersonFornavn();
        this.refusjonKontaktpersonEtternavn = avtaleInnhold.getRefusjonKontaktperson().getRefusjonKontaktpersonEtternavn();
        this.refusjonKontaktpersonTlf = avtaleInnhold.getRefusjonKontaktperson().getRefusjonKontaktpersonTlf();
        this.ønskerVarslingOmRefusjon = avtaleInnhold.getRefusjonKontaktperson().getØnskerVarslingOmRefusjon();
        this.maal = new ArrayList<>(avtaleInnhold.getMaal());
        this.inkluderingstilskuddsutgift = new ArrayList<>(avtaleInnhold.getInkluderingstilskuddsutgift());
        this.inkluderingstilskuddBegrunnelse = avtaleInnhold.getInkluderingstilskuddBegrunnelse();

        this.arbeidsgiverKontonummer = avtaleInnhold.getArbeidsgiverKontonummer();
        this.lonnstilskuddProsent = avtaleInnhold.getLonnstilskuddProsent();
        this.manedslonn = avtaleInnhold.getManedslonn();
        this.feriepengesats = avtaleInnhold.getFeriepengesats();
        this.arbeidsgiveravgift = avtaleInnhold.getArbeidsgiveravgift();
        this.otpSats = avtaleInnhold.getOtpSats();
        this.harFamilietilknytning = avtaleInnhold.getHarFamilietilknytning();
        this.familietilknytningForklaring = avtaleInnhold.getFamilietilknytningForklaring();
        this.stillingstype = avtaleInnhold.getStillingstype();

        this.mentorFornavn = avtaleInnhold.getMentorFornavn();
        this.mentorEtternavn = avtaleInnhold.getMentorEtternavn();
        this.mentorOppgaver = avtaleInnhold.getMentorOppgaver();
        this.mentorAntallTimer = avtaleInnhold.getMentorAntallTimer();
        this.mentorTlf = avtaleInnhold.getMentorTlf();
        this.mentorTimelonn = avtaleInnhold.getMentorTimelonn();

        this.vtao = Optional.ofNullable(avtale.getGjeldendeInnhold().getVtao())
                .map(VtaoFelter::new)
                .orElse(null);
    }

    public RefusjonKontaktperson getRefusjonKontaktperson() {
        if (refusjonKontaktpersonTlf == null && refusjonKontaktpersonFornavn == null && refusjonKontaktpersonEtternavn == null) {
            return null;
        }

        return new RefusjonKontaktperson(
                refusjonKontaktpersonFornavn,
                refusjonKontaktpersonEtternavn,
                refusjonKontaktpersonTlf,
                ønskerVarslingOmRefusjon
        );
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
}
