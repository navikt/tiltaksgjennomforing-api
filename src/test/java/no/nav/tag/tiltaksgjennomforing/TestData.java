package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;

import java.time.LocalDateTime;

public class TestData {
    static Avtale minimalAvtale() {
        Fnr deltakerFnr = new Fnr("12345678901");
        NavIdent veilderNavIdent = new NavIdent("X123456");
        return Avtale.nyAvtale(deltakerFnr, veilderNavIdent);
    }

    static Avtale lagAvtale() {
        return minimalAvtale().toBuilder()
                .id(1)
                .opprettetTidspunkt(LocalDateTime.of(1, 1, 1, 1, 1))
                .deltakerFornavn("Donald")
                .deltakerEtternavn("Duck")
                .build();
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
