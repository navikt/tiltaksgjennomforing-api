package no.nav.tag.tiltaksgjennomforing;

import java.time.LocalDateTime;
import java.util.Collections;

public class TestData {
    static Avtale lagAvtale() {
        return Avtale.builder()
                .id(1)
                .opprettetTidspunkt(LocalDateTime.of(1, 1, 1, 1, 1))
                .deltakerFornavn("Donald")
                .deltakerEtternavn("Duck")
                .deltakerFnr(new Fnr("12345678901"))
                .oppgaver(Collections.emptyList())
                .maal(Collections.emptyList())
                .build();
    }

    static Oppgave lagOppgave() {
        Oppgave oppgave = new Oppgave(1, LocalDateTime.of(1, 1, 1, 1, 1));
        oppgave.setTittel("Tittel");
        oppgave.setBeskrivelse("Beksrivelse");
        oppgave.setOpplaering("Opplæring");
        oppgave.setAvtale(1);
        return oppgave;
    }

/*    private static Avtale lagAvtaleMedId() {
        Avtale avtale = new Avtale();
        avtale.setId(1);
        avtale.setOpprettetTidspunkt(LocalDateTime.of(1, 1, 1, 1, 1));
        avtale.setDeltakerFornavn("Donald");
        avtale.setDeltakerEtternavn("Duck");
        avtale.setOppgaver(Collections.emptyList());
        avtale.setMaal(Collections.emptyList());
        return avtale;
    }*/

    static Maal lagMaal() {
        Maal maal = new Maal(1, LocalDateTime.of(1, 1, 1, 1, 1));
        maal.setAvtale(1);
        maal.setBeskrivelse("Beksrivelse");
        maal.setKategori("Kategori");
        return maal;
    }
}
