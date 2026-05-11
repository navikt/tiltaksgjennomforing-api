package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;

public class MentorStartOgSluttDatoStrategy extends StartOgSluttDatoStrategy {
    public MentorStartOgSluttDatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttDato(startDato, sluttDato);

        if (sluttDato == null) {
            return;
        }
        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (deltakerFnr != null && deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (startDato == null) {
            return;
        }

        boolean erOpprettetEllerEndretAvArena = avtale.erOpprettetEllerEndretAvArena();
        if (erOpprettetEllerEndretAvArena && sluttDato.isBefore(Now.localDate())){
            return;
        }

        Kvalifiseringsgruppe kvalifiseringsgruppe = avtale.getKvalifiseringsgruppe();
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
