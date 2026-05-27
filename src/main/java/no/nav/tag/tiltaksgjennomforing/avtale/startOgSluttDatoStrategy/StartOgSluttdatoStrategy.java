package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;

public class StartOgSluttdatoStrategy {
    protected Avtale avtale;

    public StartOgSluttdatoStrategy(Avtale avtale) {
        this.avtale = avtale;
    }

    public static StartOgSluttdatoStrategy create(Avtale avtale) {
        return switch (avtale.getTiltakstype()) {
            case ARBEIDSTRENING -> new ArbeidstreningStartOgSluttdatoStrategy(avtale);
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddStartOgSluttdatoStrategy(avtale);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddStartOgSluttdatoStrategy(avtale);
            case MENTOR -> new MentorStartOgSluttdatoStrategy(avtale);
            case INKLUDERINGSTILSKUDD -> new InkluderingstilskuddStartOgSluttdatoStrategy(avtale);
            case SOMMERJOBB -> new SommerjobbStartOgSluttdatoStrategy(avtale);
            case VTAO -> new VtaoStartOgSluttdatoStrategy(avtale);
            case FIREARIG_LONNSTILSKUDD -> new FirearigLonnstilskuddStartOgSluttdatoStrategy(avtale);
        };
    }

    public final void sjekkSluttdato(LocalDate sluttdato) {
        sjekkStartOgSluttdato(avtale.getGjeldendeInnhold().getStartDato(), sluttdato);
    }

    public final void sjekkGjeldendeStartogSluttdato() {
        sjekkStartOgSluttdato(avtale.getGjeldendeInnhold().getStartDato(), avtale.getGjeldendeInnhold().getSluttDato());
    }

    public final void sjekkGjeldendeStartogSluttdato(Stillingstype stillingstype) {
        sjekkStartOgSluttdato(stillingstype, avtale.getGjeldendeInnhold().getStartDato(), avtale.getGjeldendeInnhold().getSluttDato());
    }

    public final void sjekkStartOgSluttdato(LocalDate startDato, LocalDate sluttDato) {
        sjekkStartOgSluttdato(avtale.getGjeldendeInnhold().getStillingstype(), startDato, sluttDato);
    }

    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.isAfter(sluttDato)) {
            throw new FeilkodeException(Feilkode.START_ETTER_SLUTT);
        }
        if (startDato != null && !avtale.isGodkjentForEtterregistrering() && startDato.plusDays(7).isBefore(Now.localDate()) && !avtale.erAvtaleInngått()){
            throw new FeilkodeException(Feilkode.FORTIDLIG_STARTDATO);
        }
        if (sluttDato != null && sluttDato.isAfter(LocalDate.of(2089, 12, 31))) {
            throw new FeilkodeException(Feilkode.SLUTTDATO_GRENSE_NÅDD);
        }
    }
}
