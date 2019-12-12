package no.nav.tag.tiltaksgjennomforing.journalfoering;

import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.util.ArrayList;
import java.util.List;

public class AvtaleTilJournalfoeringMapper {

    public static AvtaleTilJournalfoering tilJournalfoering(AvtaleInnhold avtaleInnhold) {
        Avtale avtale = avtaleInnhold.getAvtale();
        if (!avtale.erArbeidstrening() ) {
            return null;
        }

        AvtaleTilJournalfoering avtaleTilJournalfoering = new AvtaleTilJournalfoering();

        avtaleTilJournalfoering.setGodkjentAvArbeidsgiver(avtaleInnhold.getGodkjentAvArbeidsgiver().toLocalDate());
        avtaleTilJournalfoering.setGodkjentAvVeileder(avtaleInnhold.getGodkjentAvVeileder().toLocalDate());
        avtaleTilJournalfoering.setGodkjentAvDeltaker(avtaleInnhold.getGodkjentAvDeltaker().toLocalDate());
        avtaleTilJournalfoering.setOpprettet(avtale.getOpprettetTidspunkt().toLocalDate());
        avtaleTilJournalfoering.setAvtaleId(avtale.getId());
        avtaleTilJournalfoering.setAvtaleVersjonId(avtaleInnhold.getId());
        avtaleTilJournalfoering.setDeltakerFnr(identifikatorAsString(avtale.getDeltakerFnr()));
        avtaleTilJournalfoering.setBedriftNr(identifikatorAsString(avtale.getBedriftNr()));
        avtaleTilJournalfoering.setVeilederNavIdent(identifikatorAsString(avtale.getVeilederNavIdent()));
        avtaleTilJournalfoering.setDeltakerFornavn(avtaleInnhold.getDeltakerFornavn());
        avtaleTilJournalfoering.setDeltakerEtternavn(avtaleInnhold.getDeltakerEtternavn());
        avtaleTilJournalfoering.setDeltakerTlf(avtaleInnhold.getDeltakerTlf());
        avtaleTilJournalfoering.setBedriftNavn(avtaleInnhold.getBedriftNavn());
        avtaleTilJournalfoering.setArbeidsgiverFornavn(avtaleInnhold.getArbeidsgiverFornavn());
        avtaleTilJournalfoering.setArbeidsgiverEtternavn(avtaleInnhold.getArbeidsgiverEtternavn());
        avtaleTilJournalfoering.setArbeidsgiverTlf(avtaleInnhold.getArbeidsgiverTlf());
        avtaleTilJournalfoering.setVeilederFornavn(avtaleInnhold.getVeilederFornavn());
        avtaleTilJournalfoering.setVeilederEtternavn(avtaleInnhold.getVeilederEtternavn());
        avtaleTilJournalfoering.setVeilederTlf(avtaleInnhold.getVeilederTlf());
        avtaleTilJournalfoering.setOppfolging(avtaleInnhold.getOppfolging());
        avtaleTilJournalfoering.setTilrettelegging(avtaleInnhold.getTilrettelegging());
        avtaleTilJournalfoering.setStartDato(avtaleInnhold.getStartDato());
        avtaleTilJournalfoering.setSluttDato(avtaleInnhold.getSluttDato());
        avtaleTilJournalfoering.setStillingprosent(avtaleInnhold.getStillingprosent());
        avtaleTilJournalfoering.setMaal(maalListToMaalTilJournalfoeringList(avtaleInnhold.getMaal()));
        avtaleTilJournalfoering.setOppgaver(oppgaveListToOppgaveTilJournalFoeringList(avtaleInnhold.getOppgaver()));
        avtaleTilJournalfoering.setGodkjentPaVegneGrunn(godkjentPaVegneGrunn(avtaleInnhold.getGodkjentPaVegneGrunn()));
        avtaleTilJournalfoering.setGodkjentPaVegneAv(avtaleInnhold.isGodkjentPaVegneAv());
        avtaleTilJournalfoering.setVersjon(avtaleInnhold.getVersjon());
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
