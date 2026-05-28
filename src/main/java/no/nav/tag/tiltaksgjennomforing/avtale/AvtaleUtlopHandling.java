package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.utils.DatoUtils;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public enum AvtaleUtlopHandling {
    VARSEL_EN_UKE,
    VARSEL_24_TIMER,
    UTLOP,
    INGEN;

    private static final Duration EN_DAG = Duration.ofDays(1);
    private static final Duration EN_UKE = EN_DAG.multipliedBy(7);
    private static final Duration TOLV_UKER = EN_UKE.multipliedBy(12);

    private static final Instant TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_MENTOR = LocalDate.of(2026, 6, 15)
        .atTime(LocalTime.NOON)
        .atZone(ZoneId.systemDefault())
        .toInstant();

    public static AvtaleUtlopHandling parse(Avtale avtale) {
        var sistEndretPluss12Uker = avtale.getSistEndret().plus(TOLV_UKER);
        if (Tiltakstype.MENTOR.equals(avtale.getTiltakstype()) && avtale.erOpprettetEllerEndretAvArena()) {
            var datoForRydding = DatoUtils.maksDato(sistEndretPluss12Uker, TIDLIGEST_DATO_FOR_RYDDING_AV_ARENA_MENTOR);
            return parse(datoForRydding);
        }
        return parse(sistEndretPluss12Uker);
    }

    private static AvtaleUtlopHandling parse(Instant datoForRydding) {
        if (datoForRydding.isBefore(Now.instant())) {
            return AvtaleUtlopHandling.UTLOP;
        }
        if (datoForRydding.minus(EN_DAG).isBefore(Now.instant())) {
            return AvtaleUtlopHandling.VARSEL_24_TIMER;
        }
        if (
            datoForRydding.minus(EN_UKE).isBefore(Now.instant()) &&
            datoForRydding.minus(EN_UKE).plus(EN_DAG).isAfter(Now.instant())
        ) {
            return AvtaleUtlopHandling.VARSEL_EN_UKE;
        }
        return AvtaleUtlopHandling.INGEN;
    }
}
