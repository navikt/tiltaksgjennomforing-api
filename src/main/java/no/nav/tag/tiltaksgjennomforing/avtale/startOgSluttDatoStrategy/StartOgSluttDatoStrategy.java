package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;

public class StartOgSluttDatoStrategy {
    protected Avtale avtale;

    public StartOgSluttDatoStrategy(Avtale avtale) {
        this.avtale = avtale;
    }

    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
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
