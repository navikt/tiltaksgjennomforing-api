package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VarslbarHendelseType {
    OPPRETTET("Avtale er opprettet av NAV-veileder"),
    GODKJENT_AV_ARBEIDSGIVER("Avtale er godkjent av arbeidsgiver"),
    GODKJENT_AV_VEILEDER("Avtale er godkjent av NAV-veileder"),
    GODKJENT_AV_DELTAKER("Avtale er godkjent av deltaker"),
    GODKJENT_PAA_VEGNE_AV("Avtale er godkjent av NAV-veileder"),
    GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER("Avtalens godkjenninger er opphevet av arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_VEILEDER("Avtalens godkjenninger er opphevet av NAV-veileder"),
    SMS_VARSLING_FEILET("Varsling på SMS har feilet"),
    ENDRET("Avtale endret"),
    DELT_MED_DELTAKER("Avtale delt med deltaker"),
    DELT_MED_ARBEIDSGIVER("Avtale delt med arbeidsgiver"),
    AVBRUTT("Avtale avbrutt av veileder"),
    LÅST_OPP("Avtale låst opp av veileder"),
    GJENOPPRETTET("Avtale gjenopprettet"),
    OPPRETTET_AV_ARBEIDSGIVER("Avtale er opprettet av arbeidsgiver"),
    NY_VEILEDER("Avtale tildelt ny veileder"),
    AVTALE_FORDELT("Avtale tildelt veileder"),
    TILSKUDDSPERIODE_AVSLATT("her trenger vi noe ny tekst med parameter");

    private final String tekst;
}
