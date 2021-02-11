package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VarslbarHendelseType {
    OPPRETTET("Avtale er opprettet av veileder"),
    GODKJENT_AV_ARBEIDSGIVER("Avtale er godkjent av arbeidsgiver"),
    GODKJENT_AV_VEILEDER("Avtale er godkjent av veileder"),
    GODKJENT_AV_DELTAKER("Avtale er godkjent av deltaker"),
    GODKJENT_PAA_VEGNE_AV("Avtalen ble godkjent på vegne av deltaker"),
    GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER("Avtalens godkjenninger er opphevet av arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_VEILEDER("Avtalens godkjenninger er opphevet av veileder"),
    DELT_MED_DELTAKER("Avtale delt med deltaker"),
    DELT_MED_ARBEIDSGIVER("Avtale delt med arbeidsgiver"),
    ENDRET("Avtale endret"),
    AVBRUTT("Avtale avbrutt av veileder"),
    LÅST_OPP("Avtale låst opp av veileder"),
    GJENOPPRETTET("Avtale gjenopprettet"),
    OPPRETTET_AV_ARBEIDSGIVER("Avtale er opprettet av arbeidsgiver"),
    NY_VEILEDER("Avtale tildelt ny veileder"),
    AVTALE_FORDELT("Avtale tildelt veileder"),
    TILSKUDDSPERIODE_AVSLATT("Tilskuddsperioden har blitt sendt i retur av "),
    TILSKUDDSPERIODE_GODKJENT("Tilskuddsperioden har blitt godkjent av beslutter");

    private final String tekst;

}
