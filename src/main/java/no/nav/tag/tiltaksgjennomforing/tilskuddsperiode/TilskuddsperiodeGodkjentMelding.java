package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.DatoUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Melding om godkjent tilskuddsperiode. JSON-en er flat, og {@code tiltakstype} avgjør hvilke felt som er med.
 * Mottakerne (tiltak-refusjon-api og tiltak-okonomi) deserialiserer til subtype basert på {@code tiltakstype}.
 */
public sealed interface TilskuddsperiodeGodkjentMelding {

    Felles felles();

    record Felles(
        UUID avtaleId,
        UUID tilskuddsperiodeId,
        UUID avtaleInnholdId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate avtaleFom,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate avtaleTom,
        Tiltakstype tiltakstype,
        String deltakerFornavn,
        String deltakerEtternavn,
        Identifikator deltakerFnr,
        String arbeidsgiverFornavn,
        String arbeidsgiverEtternavn,
        String arbeidsgiverTlf,
        NavIdent veilederNavIdent,
        String bedriftNavn,
        BedriftNr bedriftNr,
        @JsonProperty("tilskuddsbeløp") Integer tilskuddsbelop,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tilskuddFom,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tilskuddTom,
        Integer avtaleNr,
        @JsonProperty("løpenummer") Integer lopenummer,
        Integer resendingsnummer,
        String enhet,
        NavIdent beslutterNavIdent,
        String veilederFornavn,
        String veilederEtternavn,
        LocalDateTime godkjentTidspunkt,
        String arbeidsgiverKontonummer,
        String arbeidsgiverKid
    ) {}

    /** Midlertidig, varig og fireårig lønnstilskudd og sommerjobb. */
    record Lonnstilskudd(
        @JsonUnwrapped Felles felles,
        Double feriepengerSats,
        Double otpSats,
        Double arbeidsgiveravgiftSats,
        @JsonProperty("lønnstilskuddsprosent") Integer lonnstilskuddsprosent
    ) implements TilskuddsperiodeGodkjentMelding {}

    record Mentor(
        @JsonUnwrapped Felles felles,
        Double feriepengerSats,
        Double otpSats,
        Double arbeidsgiveravgiftSats,
        Integer mentorTimelonn,
        Double mentorAntallTimer
    ) implements TilskuddsperiodeGodkjentMelding {}

    record Vtao(
        @JsonUnwrapped Felles felles
    ) implements TilskuddsperiodeGodkjentMelding {}

    record Inkluderingstilskudd(
        @JsonUnwrapped Felles felles,
        Utgift inkluderingstilskuddsutgift
    ) implements TilskuddsperiodeGodkjentMelding {}

    record Utgift(
        UUID id,
        String navn,
        @JsonProperty("beløp") Integer belop,
        String beskrivelse,
        InkluderingstilskuddsutgiftType type
    ) {}

    final class Log {
        private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TilskuddsperiodeGodkjentMelding.class);

        private Log() {}
    }

    static TilskuddsperiodeGodkjentMelding create(Avtale avtale, TilskuddPeriode tilskuddsperiode, Integer resendingsnummer) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        Felles felles = lagFelles(avtale, innhold, tilskuddsperiode, resendingsnummer);
        Double feriepengerSats = innhold.getFeriepengesats() != null ? innhold.getFeriepengesats().doubleValue() : null;
        Double arbeidsgiveravgiftSats = innhold.getArbeidsgiveravgift() != null ? innhold.getArbeidsgiveravgift().doubleValue() : null;

        return switch (avtale.getTiltakstype()) {
            case MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, FIREARIG_LONNSTILSKUDD, SOMMERJOBB -> new Lonnstilskudd(
                felles,
                feriepengerSats,
                innhold.getOtpSats(),
                arbeidsgiveravgiftSats,
                tilskuddsperiode.getLonnstilskuddProsent()
            );
            case MENTOR -> new Mentor(
                felles,
                feriepengerSats,
                innhold.getOtpSats(),
                arbeidsgiveravgiftSats,
                innhold.getMentorTimelonn(),
                innhold.getMentorAntallTimer()
            );
            case VTAO -> new Vtao(felles);
            case INKLUDERINGSTILSKUDD -> new Inkluderingstilskudd(felles, finnUtgift(innhold, tilskuddsperiode));
            case ARBEIDSTRENING -> throw new IllegalStateException(
                "Tiltakstype " + avtale.getTiltakstype() + " har ikke tilskuddsperioder som godkjennes. tilskuddsperiodeId: " + tilskuddsperiode.getId()
            );
        };
    }

    private static Felles lagFelles(Avtale avtale, AvtaleInnhold innhold, TilskuddPeriode tilskuddsperiode, Integer resendingsnummer) {
        return new Felles(
            avtale.getId(),
            tilskuddsperiode.getId(),
            innhold.getId(),
            innhold.getStartDato(),
            innhold.getSluttDato(),
            avtale.getTiltakstype(),
            innhold.getDeltakerFornavn(),
            innhold.getDeltakerEtternavn(),
            avtale.getDeltakerFnr(),
            innhold.getArbeidsgiverFornavn(),
            innhold.getArbeidsgiverEtternavn(),
            innhold.getArbeidsgiverTlf(),
            avtale.getVeilederNavIdent(),
            innhold.getBedriftNavn(),
            avtale.getBedriftNr(),
            tilskuddsperiode.getBeløp(),
            tilskuddsperiode.getStartDato(),
            tilskuddsperiode.getSluttDato(),
            avtale.getAvtaleNr(),
            tilskuddsperiode.getLøpenummer(),
            resendingsnummer,
            tilskuddsperiode.getEnhet(),
            tilskuddsperiode.getGodkjentAvNavIdent(),
            innhold.getVeilederFornavn(),
            innhold.getVeilederEtternavn(),
            DatoUtils.instantTilLocalDateTime(tilskuddsperiode.getGodkjentTidspunkt()),
            innhold.getArbeidsgiverKontonummer(),
            innhold.getArbeidsgiverKid()
        );
    }

    private static Utgift finnUtgift(AvtaleInnhold innhold, TilskuddPeriode tilskuddsperiode) {
        return innhold.getInkluderingstilskuddsutgift().stream()
            .filter(utgift -> utgift.getTilskuddperiode() != null && tilskuddsperiode.getId().equals(utgift.getTilskuddperiode().getId()))
            .findFirst()
            .map(utgift -> new Utgift(utgift.getId(), utgift.getNavn(), utgift.getBeløp(), utgift.getBeskrivelse(), utgift.getType()))
            .orElseGet(() -> {
                // Perioder fra før utgiftene ble koblet til tilskuddsperiode. Meldingen sendes likevel, uten utgift.
                Log.LOG.warn("Fant ingen inkluderingstilskuddsutgift for tilskuddsperiode {}. Sender melding uten utgift.", tilskuddsperiode.getId());
                return null;
            });
    }
}
