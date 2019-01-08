package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;

import java.time.LocalDateTime;

public class TestData {
    static Avtale minimalAvtale() {
        NavIdent veilderNavIdent = new NavIdent("X123456");
        return Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
    }

    static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("12345678901");
        return new OpprettAvtale(deltakerFnr);
    }

    static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    static Avtale lagAvtale() {
        Avtale avtale = minimalAvtale();
        avtale.setId(1);
        avtale.setDeltakerFornavn("Donald");
        avtale.setDeltakerEtternavn("Duck");
        return avtale;
    }

    static Oppgave lagOppgave() {
        Oppgave oppgave = new Oppgave();
        oppgave.setId(1);
        oppgave.setOpprettetTidspunkt(LocalDateTime.now());
        oppgave.setTittel("Tittel");
        oppgave.setBeskrivelse("Beksrivelse");
        oppgave.setOpplaering("Opplæring");
        return oppgave;
    }

    static Maal lagMaal() {
        Maal maal = new Maal();
        maal.setId(1);
        maal.setOpprettetTidspunkt(LocalDateTime.now());
        maal.setBeskrivelse("Beksrivelse");
        maal.setKategori("Kategori");
        return maal;
    }
}
