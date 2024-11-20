package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleQueryParameterTest {

    @Test
    void ignorerer_ugyldig_felt_i_prediacate() {
        FilterSok filterSok = new FilterSok();
        filterSok.setQueryParametre("""
                {"avtaleNr": 1, "avtaleTulleNummer": 1337}
                """);
        AvtaleQueryParameter avtalePredicate = filterSok.getAvtalePredicate();
        assertThat(avtalePredicate.getAvtaleNr()).isEqualTo(1);
    }

    @Test
    void ignorerer_ugyldig_json_i_prediacate() {
        FilterSok filterSok = new FilterSok();
        filterSok.setQueryParametre("""
                {
                """);
        AvtaleQueryParameter avtalePredicate = filterSok.getAvtalePredicate();
        assertThat(avtalePredicate).isEqualTo(new AvtaleQueryParameter());
    }
}
