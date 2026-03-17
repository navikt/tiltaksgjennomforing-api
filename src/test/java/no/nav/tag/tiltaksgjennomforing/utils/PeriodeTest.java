package no.nav.tag.tiltaksgjennomforing.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodeTest {

    private static final LocalDate JAN_1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate JAN_31 = LocalDate.of(2024, 1, 31);

    @Test
    public void split__ingen_split_dato_gir_opprinnelig_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split(Collections.emptyList());

        assertThat(result).containsExactly(new Periode(JAN_1, JAN_31));
    }

    @Test
    public void split__med_ingenting_gir_opprinnelig_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split();

        assertThat(result).containsExactly(new Periode(JAN_1, JAN_31));
    }

    @Test
    public void split__dato_utenfor_periode_ignoreres() {
        Periode periode = new Periode(JAN_1, JAN_31);
        LocalDate utenfor = LocalDate.of(2024, 2, 15);

        List<Periode> result = periode.split(utenfor);

        assertThat(result).containsExactly(new Periode(JAN_1, JAN_31));
    }

    @Test
    public void split__en_dato_i_midten_gir_to_perioder() {
        Periode periode = new Periode(JAN_1, JAN_31);
        LocalDate midt = LocalDate.of(2024, 1, 15);

        List<Periode> result = periode.split(midt);

        assertThat(result).containsExactly(
            new Periode(JAN_1, LocalDate.of(2024, 1, 14)),
            new Periode(midt, JAN_31)
        );
    }

    @Test
    public void split__to_datoer_gir_tre_perioder() {
        Periode periode = new Periode(JAN_1, JAN_31);
        LocalDate dato1 = LocalDate.of(2024, 1, 10);
        LocalDate dato2 = LocalDate.of(2024, 1, 20);

        List<Periode> result = periode.split(dato1, dato2);

        assertThat(result).containsExactly(
            new Periode(JAN_1, LocalDate.of(2024, 1, 9)),
            new Periode(dato1, LocalDate.of(2024, 1, 19)),
            new Periode(dato2, JAN_31)
        );
    }

    @Test
    public void split__split_paa_startdato_gir_opprinnelig_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split(JAN_1);

        assertThat(result).containsExactly(new Periode(JAN_1, JAN_31));
    }

    @Test
    public void split__split_paa_sluttdato_gir_to_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split(JAN_31);

        assertThat(result).containsExactly(
            new Periode(JAN_1, JAN_31.minusDays(1)),
            new Periode(JAN_31, JAN_31)
        );
    }

    @Test
    public void split__duplikate_datoer_gir_ikke_ekstra_perioder() {
        Periode periode = new Periode(JAN_1, JAN_31);
        LocalDate dato = LocalDate.of(2024, 1, 15);

        List<Periode> result = periode.split(dato, dato);

        assertThat(result).containsExactly(
            new Periode(JAN_1, LocalDate.of(2024, 1, 14)),
            new Periode(dato, JAN_31)
        );
    }

    @Test
    public void split__datoer_i_usortert_rekkefolge_sorteres_korrekt() {
        Periode periode = new Periode(JAN_1, JAN_31);
        LocalDate dato1 = LocalDate.of(2024, 1, 20);
        LocalDate dato2 = LocalDate.of(2024, 1, 10);

        List<Periode> result = periode.split(dato1, dato2);

        assertThat(result).containsExactly(
            new Periode(JAN_1, LocalDate.of(2024, 1, 9)),
            new Periode(dato2, LocalDate.of(2024, 1, 19)),
            new Periode(dato1, JAN_31)
        );
    }

    @Test
    public void split__en_dags_periode_uten_split_gir_en_periode() {
        LocalDate enDag = LocalDate.of(2024, 1, 15);
        Periode periode = new Periode(enDag, enDag);

        List<Periode> result = periode.split();

        assertThat(result).containsExactly(new Periode(enDag, enDag));
    }

    @Test
    public void split__start_skuddaar() {
        LocalDate start = LocalDate.of(2024, 2, 29);
        LocalDate slutt = LocalDate.of(2028, 2, 27);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.split(
            LocalDate.of(2025, 2, 28),
            LocalDate.of(2026, 2, 28),
            LocalDate.of(2027, 2, 28)
        );

        assertThat(result).containsExactly(
            Periode.av(LocalDate.of(2024,2,29), LocalDate.of(2025,2,27)),
            Periode.av(LocalDate.of(2025,2,28), LocalDate.of(2026,2,27)),
            Periode.av(LocalDate.of(2026,2,28), LocalDate.of(2027,2,27)),
            Periode.av(LocalDate.of(2027,2,28), LocalDate.of(2028,2,27))
        );
    }


    @Test
    public void splitPerMnd__hel_maned_gir_en_periode() {
        Periode periode = Periode.av(JAN_1, JAN_31);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).containsExactly(Periode.av(JAN_1, JAN_31));
    }

    @Test
    public void splitPerMnd__en_dags_periode_gir_en_periode() {
        LocalDate enDag = LocalDate.of(2024, 3, 15);
        Periode periode = Periode.av(enDag, enDag);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).containsExactly(Periode.av(enDag, enDag));
    }

    @Test
    public void splitPerMnd__start_og_slutt_i_ulike_maneder_gir_riktig_antall_perioder() {
        // 20. jan → 10. mar = 3 perioder (jan, feb, mar)
        LocalDate start = LocalDate.of(2024, 1, 20);
        LocalDate slutt = LocalDate.of(2024, 3, 10);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).hasSize(3);
    }

    @Test
    public void splitPerMnd__foerste_periode_starter_paa_oppgitt_startdato() {
        LocalDate start = LocalDate.of(2024, 1, 20);
        LocalDate slutt = LocalDate.of(2024, 3, 10);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result.getFirst().getStart()).isEqualTo(start);
    }

    @Test
    public void splitPerMnd__siste_periode_slutter_paa_oppgitt_sluttdato() {
        LocalDate start = LocalDate.of(2024, 1, 20);
        LocalDate slutt = LocalDate.of(2024, 3, 10);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result.getLast().getSlutt()).isEqualTo(slutt);
    }

    @Test
    public void splitPerMnd__mellomliggende_perioder_dekker_hele_maneder() {
        // jan 20 → mar 10: februar-perioden skal dekke hele februar
        LocalDate start = LocalDate.of(2024, 1, 20);
        LocalDate slutt = LocalDate.of(2024, 3, 10);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        Periode februar = result.get(1);
        assertThat(februar.getStart()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(februar.getSlutt()).isEqualTo(LocalDate.of(2024, 2, 29)); // 2024 er skuddår
    }

    @Test
    public void splitPerMnd__krysser_arsskifte() {
        // 15. nov 2023 → 15. feb 2024 = 4 perioder
        LocalDate start = LocalDate.of(2023, 11, 15);
        LocalDate slutt = LocalDate.of(2024, 2, 15);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).containsExactly(
            Periode.av(LocalDate.of(2023, 11, 15), LocalDate.of(2023, 11, 30)),
            Periode.av(LocalDate.of(2023, 12, 1),  LocalDate.of(2023, 12, 31)),
            Periode.av(LocalDate.of(2024, 1, 1),   LocalDate.of(2024, 1, 31)),
            Periode.av(LocalDate.of(2024, 2, 1),   LocalDate.of(2024, 2, 15))
        );
    }

    @Test
    public void splitPerMnd__noyaktig_to_hele_maneder() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate slutt = LocalDate.of(2024, 3, 31);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).containsExactly(
            Periode.av(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)),
            Periode.av(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31))
        );
    }

    @Test
    public void splitPerMnd__start_siste_dag_i_maned_gir_en_periode() {
        LocalDate start = LocalDate.of(2024, 1, 31);
        LocalDate slutt = LocalDate.of(2024, 1, 31);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).containsExactly(Periode.av(start, slutt));
    }

    @Test
    public void splitPerMnd__ett_helt_aar_gir_tolv_perioder() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate slutt = LocalDate.of(2024, 12, 31);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).hasSize(12);
        // Kontroller at alle 12 måneder er representert
        for (int mnd = 1; mnd <= 12; mnd++) {
            int finalMnd = mnd;
            assertThat(result).anyMatch(p -> p.getStart().getMonthValue() == finalMnd);
        }
    }

    @Test
    public void splitPerMnd__ingen_perioder_overlapper() {
        LocalDate start = LocalDate.of(2023, 6, 15);
        LocalDate slutt = LocalDate.of(2024, 4, 20);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        // Sluttdato for periode N skal alltid være dagen før startdato for periode N+1
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getSlutt().plusDays(1))
                .isEqualTo(result.get(i + 1).getStart());
        }
    }

    @Test
    public void splitPerMnd__start_skuddaar() {
        LocalDate start = LocalDate.of(2024, 2, 29);
        LocalDate slutt = LocalDate.of(2025, 2, 28);
        Periode periode = Periode.av(start, slutt);

        List<Periode> result = periode.splitPerMnd();

        assertThat(result).containsExactly(
            Periode.av(LocalDate.of(2024,2,29), LocalDate.of(2024,2,29)),
            Periode.av(LocalDate.of(2024,3,1), LocalDate.of(2024,3,31)),
            Periode.av(LocalDate.of(2024,4,1), LocalDate.of(2024,4,30)),
            Periode.av(LocalDate.of(2024,5,1), LocalDate.of(2024,5,31)),
            Periode.av(LocalDate.of(2024,6,1), LocalDate.of(2024,6,30)),
            Periode.av(LocalDate.of(2024,7,1), LocalDate.of(2024,7,31)),
            Periode.av(LocalDate.of(2024,8,1), LocalDate.of(2024,8,31)),
            Periode.av(LocalDate.of(2024,9,1), LocalDate.of(2024,9,30)),
            Periode.av(LocalDate.of(2024,10,1), LocalDate.of(2024,10,31)),
            Periode.av(LocalDate.of(2024,11,1), LocalDate.of(2024,11,30)),
            Periode.av(LocalDate.of(2024,12,1), LocalDate.of(2024,12,31)),
            Periode.av(LocalDate.of(2025,1,1), LocalDate.of(2025,1,31)),
            Periode.av(LocalDate.of(2025,2,1), LocalDate.of(2025,2,28))
        );
    }
}
