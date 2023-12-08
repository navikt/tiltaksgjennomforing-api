package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.HashMap;
import java.util.Map;

public class ArbeidsgiverAvgiftEkstra {
    private static final int arbeidsgiveravgiftEsktra_2023 = 750000;
    private static final int arbeidsgiveravgiftEsktra_2024 = 850000;
    private static Map<Integer, Integer> arbeidsgiveravgiftEkstra;
    static {
        Map<Integer, Integer> arbeidsgiveravgiftEkstra = new HashMap<>();
        arbeidsgiveravgiftEkstra.put(2023, arbeidsgiveravgiftEsktra_2023);
        arbeidsgiveravgiftEkstra.put(2024, arbeidsgiveravgiftEsktra_2024);
    }

    public static int getArbeidsgiveravgiftEsktraForPeriode(TilskuddPeriode tilskuddPeriode) {
//        int startDato = tilskuddPeriode.getStartDato().getYear();
//        int sluttDato = tilskuddPeriode.getSluttDato().getYear();
//        if (startDato == sluttDato) { // burde ikke skje da vi ikke tillater perioder som går over årsskifte
//            // start og sluttdato er i samme år
//            return arbeidsgiveravgiftEkstra(startDato);
//        }
//        int arbeidsgiverAvgiftEkstra = arbeidsgiveravgiftEkstra(tilskuddPeriode.getSluttDato().getYear());
//
//        return arbeidsgiverAvgiftEkstra;

        if (tilskuddPeriode.getSluttDato().getYear() == 2023) {
            return arbeidsgiveravgiftEsktra_2023;
        } else if (tilskuddPeriode.getSluttDato().getYear() == 2024) {
            return arbeidsgiveravgiftEsktra_2024;
        } else {
            return 0;
        }
    }

    public static int arbeidsgiveravgiftEkstra(int year) {
        if (ArbeidsgiverAvgiftEkstra.arbeidsgiveravgiftEkstra.get(year) == null) {
            throw new RuntimeException("Arbeidsgiveravgift ekstra ikke satt for år " + year);
        } else
            return arbeidsgiveravgiftEkstra(year);

    }
}
