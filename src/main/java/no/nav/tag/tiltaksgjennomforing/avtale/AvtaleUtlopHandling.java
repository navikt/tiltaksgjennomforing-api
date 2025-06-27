package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Duration;
import java.time.Instant;

public enum AvtaleUtlopHandling {
    VARSEL_EN_UKE,
    VARSEL_24_TIMER,
    UTLOP,
    INGEN;

    public static final Instant TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_VTAO = Instant.parse("2025-09-01T12:00:00.000+01:00");

    private static final Duration EN_DAG = Duration.ofDays(1);
    private static final Duration EN_UKE = EN_DAG.multipliedBy(7);
    private static final Duration TOLV_UKER = EN_UKE.multipliedBy(12);
    private static final Duration ELLEVE_UKER = EN_UKE.multipliedBy(11);

    public static AvtaleUtlopHandling parse(Avtale avtale) {
        if (Tiltakstype.VTAO.equals(avtale.getTiltakstype()) && Avtaleopphav.ARENA.equals(avtale.getOpphav())) {
            var sistEndret = Now.instant().isBefore(TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_VTAO)
                ? TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_VTAO.minus(TOLV_UKER)
                : avtale.getSistEndret();
            return parse(sistEndret);
        }
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
