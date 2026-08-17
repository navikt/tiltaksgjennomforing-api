package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;

public class MentorStartOgSluttdatoStrategy extends StartOgSluttdatoStrategy {
    public MentorStartOgSluttdatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttdato(stillingstype, startDato, sluttDato);

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

        Innsatsgruppe innsatsgruppe = avtale.getInnsatsgruppe();
        boolean erNedsattArbeidsevne = innsatsgruppe == Innsatsgruppe.TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE;
        boolean erLitenMulighetEllerJobbeDelvis = Innsatsgruppe.LITEN_MULIGHET_ELLER_JOBBE_DELVIS.contains(innsatsgruppe);

        if ((erNedsattArbeidsevne || erLitenMulighetEllerJobbeDelvis) && startDato.plusMonths(36).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MENTOR_36_MND);
        }
        if (!erNedsattArbeidsevne && !erLitenMulighetEllerJobbeDelvis && startDato.plusMonths(6).minusDays(1).isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MENTOR_6_MND);
        }

    }
}
