package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangFirearigLonnstilskuddException;

import java.time.LocalDate;

public class FirearigLonnstilskuddStartOgSluttDatoStrategy extends StartOgSluttDatoStrategy {
    private static final int MAKS_VARIGHET_MIDLERTIDIG_STILLING_ANTALL_AAR = 2;
    private static final int MAKS_VARIGHET_FAST_STILLING_ANTALL_AAR = 4;

    private final LocalDate firearigOppstartsdato;
    public FirearigLonnstilskuddStartOgSluttDatoStrategy(Avtale avtale) {
        super(avtale);
        this.firearigOppstartsdato = FirearigLonnstilskuddProperties.getInstance().getDato();
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttDato(startDato, sluttDato);

        if (startDato == null) {
            return;
        }
        if (startDato.isBefore(firearigOppstartsdato)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_TIDLIG_OPPSTART);
        }
        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (deltakerFnr != null && deltakerFnr.erOver30årFraOppstartDato(startDato)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }
        if (sluttDato == null) {
            return;
        }
        if (sluttDato.isAfter(LocalDate.of(2032, 12, 31))) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_SEN_SLUTTDATO);
        }
        Stillingstype stillingstype = avtale.getGjeldendeInnhold().getStillingstype();
        if (erForLangVarighet(stillingstype, startDato, sluttDato)) {
            throw new VarighetForLangFirearigLonnstilskuddException(stillingstype);
        }
    }

    public static boolean erForLangVarighet(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        if (startDato == null || sluttDato == null) {
            return false;
        }
        boolean erMidlertidigStilling = Stillingstype.MIDLERTIDIG.equals(stillingstype);
        int maksVarighetAar = erMidlertidigStilling
            ? MAKS_VARIGHET_MIDLERTIDIG_STILLING_ANTALL_AAR
            : MAKS_VARIGHET_FAST_STILLING_ANTALL_AAR;
        return !sluttDato.isBefore(startDato.plusYears(maksVarighetAar));
    }
}
