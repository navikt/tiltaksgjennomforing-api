package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;

public class TestData {
    static Avtale minimalAvtale() {
        Fnr deltakerFnr = new Fnr("12345678901");
        NavIdent veilderNavIdent = new NavIdent("X123456");
        return Avtale.nyAvtale(new OpprettAvtale(deltakerFnr, veilderNavIdent));
    }

    static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    static Avtale lagAvtale() {
        Avtale avtale = minimalAvtale();
        avtale.setDeltakerFornavn("Donald");
        avtale.setDeltakerEtternavn("Duck");
        return avtale;
    }

    static Oppgave lagOppgave() {
        Oppgave oppgave = new Oppgave();
        oppgave.settIdOgOpprettetTidspunkt();
        oppgave.setTittel("Tittel");
        oppgave.setBeskrivelse("Beksrivelse");
        oppgave.setOpplaering("Opplæring");
        return oppgave;
    }

    static Maal lagMaal() {
        Maal maal = new Maal();
        maal.settIdOgOpprettetTidspunkt();
        maal.setBeskrivelse("Beksrivelse");
        maal.setKategori("Kategori");
        return maal;
    }
}
