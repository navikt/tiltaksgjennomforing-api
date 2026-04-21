package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.DatoUtils;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public enum AvtaleUtlopHandling {
    VARSEL_EN_UKE,
    VARSEL_24_TIMER,
    UTLOP,
    INGEN;

    private static final Instant TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_MENTOR = LocalDate.of(2026, 6, 1)
        .atTime(LocalTime.NOON)
        .atOffset(ZoneOffset.UTC)
        .toInstant();

    private static final Duration EN_DAG = Duration.ofDays(1);
    private static final Duration EN_UKE = EN_DAG.multipliedBy(7);
    private static final Duration TOLV_UKER = EN_UKE.multipliedBy(12);
    private static final Duration ELLEVE_UKER = EN_UKE.multipliedBy(11);

    public static AvtaleUtlopHandling parse(Avtale avtale) {
        if (Tiltakstype.MENTOR.equals(avtale.getTiltakstype()) && avtale.erOpprettetEllerEndretAvArena()) {
            var sistEndret = DatoUtils.maksDato(avtale.getSistEndret(), TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_MENTOR.minus(TOLV_UKER));
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
