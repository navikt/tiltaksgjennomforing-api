package no.nav.tag.tiltaksgjennomforing.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodeTest {

    private static final LocalDate JAN_1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate JAN_31 = LocalDate.of(2024, 1, 31);

    @Test
    public void split__ingen_split_dato_gir_opprinnelig_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split();

        assertThat(result).containsExactly(new Periode(JAN_1, JAN_31));
    }

    @Test
    public void split__med_null_gir_opprinnelig_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split(null);

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
    public void split__split_paa_sluttdato_gir_opprinnelig_periode() {
        Periode periode = new Periode(JAN_1, JAN_31);

        List<Periode> result = periode.split(JAN_31);

        assertThat(result).containsExactly(new Periode(JAN_1, JAN_31));
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
}
