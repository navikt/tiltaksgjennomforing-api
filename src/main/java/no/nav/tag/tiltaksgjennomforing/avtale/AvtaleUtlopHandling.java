package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Duration;
import java.time.Instant;

public enum AvtaleUtlopHandling {
    VARSEL_EN_UKE,
    VARSEL_24_TIMER,
    UTLOP,
    INGEN;


    private static final Duration EN_DAG = Duration.ofDays(1);
    private static final Duration EN_UKE = EN_DAG.multipliedBy(7);
    private static final Duration TOLV_UKER = EN_UKE.multipliedBy(12);
    private static final Duration ELLEVE_UKER = EN_UKE.multipliedBy(11);
    private static final Instant START_DATO_FOR_RYDDING = Instant.parse("2024-11-28T12:00:00.000+01:00");
    private static final Instant TIDLIGEST_DATO_FOR_SIST_ENDRET = START_DATO_FOR_RYDDING.minus(TOLV_UKER);

    public static AvtaleUtlopHandling parse(Avtale avtale) {
        Instant sistEndret = avtale.getSistEndret().isBefore(TIDLIGEST_DATO_FOR_SIST_ENDRET)
            ? TIDLIGEST_DATO_FOR_SIST_ENDRET
            : avtale.getSistEndret();

        return parse(sistEndret);
    }

    public static AvtaleUtlopHandling parse(Instant sistEndret) {
        if (sistEndret.plus(TOLV_UKER).isBefore(Now.instant())) {
            return AvtaleUtlopHandling.UTLOP;
        }
        if (sistEndret.plus(TOLV_UKER).minus(EN_DAG).isBefore(Now.instant())) {
            return AvtaleUtlopHandling.VARSEL_24_TIMER;
        }
        if (
            sistEndret.plus(ELLEVE_UKER).isBefore(Now.instant()) &&
                sistEndret.plus(ELLEVE_UKER).plus(EN_DAG).isAfter(Now.instant())
        ) {
            return AvtaleUtlopHandling.VARSEL_EN_UKE;
        }
        return AvtaleUtlopHandling.INGEN;
    }
}
