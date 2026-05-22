package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangFirearigLonnstilskuddException;

import java.time.LocalDate;

public class FirearigLonnstilskuddStartOgSluttdatoStrategy extends StartOgSluttdatoStrategy {
    private static final int MAKS_VARIGHET_MIDLERTIDIG_STILLING_ANTALL_AAR = 2;
    private static final int MAKS_VARIGHET_FAST_STILLING_ANTALL_AAR = 4;
    private static final LocalDate SLUTTDATO_FOR_FIREARIG_LTS_FORSOK = LocalDate.of(2032, 12, 31);

    private final LocalDate firearigOppstartsdato;

    public FirearigLonnstilskuddStartOgSluttdatoStrategy(Avtale avtale) {
        super(avtale);
        this.firearigOppstartsdato = FirearigLonnstilskuddProperties.getInstance().getDato();
    }

    @Override
    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttdato(stillingstype, startDato, sluttDato);

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
        if (sluttDato.isAfter(SLUTTDATO_FOR_FIREARIG_LTS_FORSOK)) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_SEN_SLUTTDATO);
        }
        if (erForLangVarighet(stillingstype, startDato, sluttDato)) {
            throw new VarighetForLangFirearigLonnstilskuddException(stillingstype);
        }
    }

    static boolean erForLangVarighet(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        if (startDato == null || sluttDato == null) {
            return false;
        }
        return switch (stillingstype) {
            case MIDLERTIDIG -> !sluttDato.isBefore(startDato.plusYears(MAKS_VARIGHET_MIDLERTIDIG_STILLING_ANTALL_AAR));
            case null, default -> !sluttDato.isBefore(startDato.plusYears(MAKS_VARIGHET_FAST_STILLING_ANTALL_AAR));
        };
    }
}
