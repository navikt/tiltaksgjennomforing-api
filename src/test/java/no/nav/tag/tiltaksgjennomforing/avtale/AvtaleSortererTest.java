package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AvtaleSortererTest {
    @Test
    void sorterer_liste() {
        Avtale avtale1 = TestData.enArbeidstreningAvtale();
        Avtale avtale2 = TestData.enArbeidstreningAvtale();
        Avtale avtale3 = TestData.enArbeidstreningAvtale();
        avtale1.setDeltakerFornavn("B");
        avtale2.setDeltakerFornavn("A");
        avtale3.setDeltakerFornavn(null);
        List<Avtale> usortertListe = List.of(avtale3, avtale1, avtale2);
        assertThat(AvtaleSorterer.sorterAvtaler(AvtaleInnhold.Fields.deltakerFornavn, usortertListe)).containsExactly(avtale1, avtale2, avtale3);
    }
}