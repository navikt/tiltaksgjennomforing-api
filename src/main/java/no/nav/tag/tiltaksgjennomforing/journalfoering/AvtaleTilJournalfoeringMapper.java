package no.nav.tag.tiltaksgjennomforing.journalfoering;

import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AvtaleTilJournalfoeringMapper {

    public static AvtaleTilJournalfoering tilJournalfoering(Avtale avtale) {
        if (avtale == null) {
            return null;
        }

        AvtaleTilJournalfoering avtaleTilJournalfoering = new AvtaleTilJournalfoering();

        if (avtale.getGodkjentAvArbeidsgiver() != null) {
            avtaleTilJournalfoering.setGodkjentAvArbeidsgiver(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(avtale.getGodkjentAvArbeidsgiver()));
        }
        if (avtale.getGodkjentAvVeileder() != null) {
            avtaleTilJournalfoering.setGodkjentAvVeileder(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(avtale.getGodkjentAvVeileder()));
        }
        if (avtale.getGodkjentAvDeltaker() != null) {
            avtaleTilJournalfoering.setGodkjentAvDeltaker(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(avtale.getGodkjentAvDeltaker()));
        }
        if (avtale.getOpprettetTidspunkt() != null) {
            avtaleTilJournalfoering.setOpprettet(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(avtale.getOpprettetTidspunkt()));
        }
        avtaleTilJournalfoering.setId(avtale.getId());
        avtaleTilJournalfoering.setDeltakerFnr(identifikatorAsString(avtale.getDeltakerFnr()));
        avtaleTilJournalfoering.setBedriftNr(identifikatorAsString(avtale.getBedriftNr()));
        avtaleTilJournalfoering.setVeilederNavIdent(identifikatorAsString(avtale.getVeilederNavIdent()));
        avtaleTilJournalfoering.setVersjon(avtale.getVersjon());
        avtaleTilJournalfoering.setDeltakerFornavn(avtale.getDeltakerFornavn());
        avtaleTilJournalfoering.setDeltakerEtternavn(avtale.getDeltakerEtternavn());
        avtaleTilJournalfoering.setDeltakerTlf(avtale.getDeltakerTlf());
        avtaleTilJournalfoering.setBedriftNavn(avtale.getBedriftNavn());
        avtaleTilJournalfoering.setArbeidsgiverFornavn(avtale.getArbeidsgiverFornavn());
        avtaleTilJournalfoering.setArbeidsgiverEtternavn(avtale.getArbeidsgiverEtternavn());
        avtaleTilJournalfoering.setArbeidsgiverTlf(avtale.getArbeidsgiverTlf());
        avtaleTilJournalfoering.setVeilederFornavn(avtale.getVeilederFornavn());
        avtaleTilJournalfoering.setVeilederEtternavn(avtale.getVeilederEtternavn());
        avtaleTilJournalfoering.setVeilederTlf(avtale.getVeilederTlf());
        avtaleTilJournalfoering.setOppfolging(avtale.getOppfolging());
        avtaleTilJournalfoering.setTilrettelegging(avtale.getTilrettelegging());
        avtaleTilJournalfoering.setStartDato(avtale.getStartDato());
        avtaleTilJournalfoering.setArbeidstreningLengde(avtale.getArbeidstreningLengde());
        avtaleTilJournalfoering.setArbeidstreningStillingprosent(avtale.getArbeidstreningStillingprosent());
        avtaleTilJournalfoering.setMaal(maalListToMaalTilJournalfoeringList(avtale.getMaal()));
        avtaleTilJournalfoering.setOppgaver(oppgaveListToOppgaveTilJournalFoeringList(avtale.getOppgaver()));
        avtaleTilJournalfoering.setGodkjentPaVegneGrunn(godkjentPaVegneGrunn(avtale.getGodkjentPaVegneGrunn()));
        avtaleTilJournalfoering.setGodkjentPaVegneAv(String.valueOf(avtale.isGodkjentPaVegneAv()));

        return avtaleTilJournalfoering;
    }

    private static String identifikatorAsString(Identifikator id) {
        return id.asString();
    }

    private static GodkjentPaVegneGrunnTilJournalfoering godkjentPaVegneGrunn(GodkjentPaVegneGrunn grunn) {
        if (grunn == null) {
            return null;
        }

        return new GodkjentPaVegneGrunnTilJournalfoering(
                grunn.isIkkeBankId(),
                grunn.isDigitalKompetanse(),
                grunn.isReservert()
        );
    }


    private static MaalTilJournalfoering maalToMaalTilJournalfoering(Maal maal) {
        if (maal == null) {
            return null;
        }

        MaalTilJournalfoering maalTilJournalfoering = new MaalTilJournalfoering();

        maalTilJournalfoering.setKategori(maal.getKategori());
        maalTilJournalfoering.setBeskrivelse(maal.getBeskrivelse());

        return maalTilJournalfoering;
    }

    private static List<MaalTilJournalfoering> maalListToMaalTilJournalfoeringList(List<Maal> list) {
        if (list == null) {
            return null;
        }

        List<MaalTilJournalfoering> list1 = new ArrayList<>(list.size());
        for (Maal maal : list) {
            list1.add(maalToMaalTilJournalfoering(maal));
        }

        return list1;
    }

    private static OppgaveTilJournalFoering oppgaveToOppgaveTilJournalFoering(Oppgave oppgave) {
        if (oppgave == null) {
            return null;
        }

        OppgaveTilJournalFoering oppgaveTilJournalFoering = new OppgaveTilJournalFoering();

        oppgaveTilJournalFoering.setTittel(oppgave.getTittel());
        oppgaveTilJournalFoering.setBeskrivelse(oppgave.getBeskrivelse());
        oppgaveTilJournalFoering.setOpplaering(oppgave.getOpplaering());

        return oppgaveTilJournalFoering;
    }

    private static List<OppgaveTilJournalFoering> oppgaveListToOppgaveTilJournalFoeringList(List<Oppgave> list) {
        if (list == null) {
            return null;
        }

        List<OppgaveTilJournalFoering> list1 = new ArrayList<OppgaveTilJournalFoering>(list.size());
        for (Oppgave oppgave : list) {
            list1.add(oppgaveToOppgaveTilJournalFoering(oppgave));
        }

        return list1;
    }
}
