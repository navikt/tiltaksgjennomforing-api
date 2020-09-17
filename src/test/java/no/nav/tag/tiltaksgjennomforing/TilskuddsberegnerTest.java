package no.nav.tag.tiltaksgjennomforing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TilskuddsberegnerTest {


    @Test
    public void beregnerFeriepenger(){
        BigDecimal månedslønn = BigDecimal.valueOf(24445);
        BigDecimal feriepengerSats = BigDecimal.valueOf(0.129999);

     BigDecimal resultat = Tilskuddsberegner.beregnferiepenger(månedslønn, feriepengerSats);

        System.out.println(resultat);
        assertEquals(BigDecimal.valueOf(2400), resultat);
    }


}
