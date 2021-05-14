package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

@Value
public class TilskuddsperiodeGodkjentMelding {

    UUID avtaleId;
    UUID tilskuddsperiodeId;
    UUID avtaleInnholdId;
    Tiltakstype tiltakstype;
    String deltakerFornavn;
    String deltakerEtternavn;
    Identifikator deltakerFnr;
    NavIdent veilederNavIdent;
    String bedriftNavn;
    BedriftNr bedriftNr;
    Integer tilskuddsbeløp;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate tilskuddFom;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate tilskuddTom;
    Double feriepengerSats;
    Double otpSats;
    Double arbeidsgiveravgiftSats;
    Integer lønnstilskuddsprosent;
    Integer avtaleNr;
    Integer løpenummer;
    String enhet;
    NavIdent beslutterNavIdent;

    public static TilskuddsperiodeGodkjentMelding create(Avtale avtale, TilskuddPeriode tilskuddsperiode) {
        return new TilskuddsperiodeGodkjentMelding(avtale.getId(),
                tilskuddsperiode.getId(),
                avtale.getAvtaleInnholdId(),
                avtale.getTiltakstype(),
                avtale.getDeltakerFornavn(),
                avtale.getDeltakerEtternavn(),
                avtale.getDeltakerFnr(),
                avtale.getVeilederNavIdent(),
                avtale.getBedriftNavn(),
                avtale.getBedriftNr(),
                tilskuddsperiode.getBeløp(),
                tilskuddsperiode.getStartDato(),
                tilskuddsperiode.getSluttDato(),
                avtale.getFeriepengesats().doubleValue(),
                avtale.getOtpSats(),
                avtale.getArbeidsgiveravgift().doubleValue(),
                tilskuddsperiode.getLonnstilskuddProsent(),
                avtale.getAvtaleNr(),
                tilskuddsperiode.getLøpenummer(),
                avtale.getEnhetOppfolging(),
                tilskuddsperiode.getGodkjentAvNavIdent()
        );
    }
}
