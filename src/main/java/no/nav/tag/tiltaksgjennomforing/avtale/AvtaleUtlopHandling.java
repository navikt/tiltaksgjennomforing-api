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

    public static AvtaleUtlopHandling parse(Avtale avtale) {
        return parse(avtale.getSistEndret());
    }

    private static AvtaleUtlopHandling parse(Instant sistEndret) {
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
