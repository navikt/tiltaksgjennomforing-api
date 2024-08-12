package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AvtaleSortererTest {
    @Test
    void sorterer_liste() {
        Avtale avtale1 = TestData.enArbeidstreningAvtale();
        Avtale avtale2 = TestData.enArbeidstreningAvtale();
        Avtale avtale3 = TestData.enArbeidstreningAvtale();
        avtale1.getGjeldendeInnhold().setDeltakerFornavn("B");
        avtale2.getGjeldendeInnhold().setDeltakerFornavn("A");
        avtale3.getGjeldendeInnhold().setDeltakerFornavn(null);
        List<Avtale> usortertListe = List.of(avtale3, avtale1, avtale2);
        List<Avtale> sortertListe = usortertListe.stream().sorted(AvtaleSorterer.comparatorForAvtale(AvtaleInnhold.Fields.deltakerFornavn)).collect(Collectors.toList());

        assertThat(sortertListe).containsExactly(avtale2, avtale1, avtale3);
    }
}