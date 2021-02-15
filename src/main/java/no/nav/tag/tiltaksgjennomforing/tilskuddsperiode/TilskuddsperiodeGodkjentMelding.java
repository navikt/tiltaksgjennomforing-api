package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.time.LocalDate;
import java.util.UUID;

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
                tilskuddsperiode.getLonnstilskuddProsent()
        );
    }
}
