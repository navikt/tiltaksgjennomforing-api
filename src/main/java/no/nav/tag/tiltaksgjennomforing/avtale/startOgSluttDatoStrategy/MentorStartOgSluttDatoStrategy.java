package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;


import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class MentorStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    private final Kvalifiseringsgruppe kvalifiseringsgruppe;
    private final Boolean erOpprettetEllerEndretAvArena;

    public MentorStartOgSluttDatoStrategy(Kvalifiseringsgruppe kvalifiseringsgruppe, Boolean erOpprettetEllerEndretAvArena) {
        this.kvalifiseringsgruppe = kvalifiseringsgruppe;
        this.erOpprettetEllerEndretAvArena = erOpprettetEllerEndretAvArena;
    }

    @Override
    public void sjekkStartOgSluttDato(
        LocalDate startDato,
        LocalDate sluttDato,
        boolean erGodkjentForEtterregistrering,
        boolean erAvtaleInngått,
        Fnr deltakerFnr
    ) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr);

        if (sluttDato == null) {
            return;
        }
        if (deltakerFnr != null && deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (startDato == null) {
            return;
        }

        if(erOpprettetEllerEndretAvArena && sluttDato.isBefore(LocalDate.now())){
            return;
        }

        boolean erSpesieltTilpasset = kvalifiseringsgruppe == Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS;
        boolean erVarigTilpasset = kvalifiseringsgruppe == Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS;

        if ((erSpesieltTilpasset || erVarigTilpasset) && startDato.plusMonths(36).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MENTOR_36_MND);
        }
        if (!erSpesieltTilpasset && !erVarigTilpasset && startDato.plusMonths(6).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MENTOR_6_MND);
        }

    }
}
