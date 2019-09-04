package no.nav.tag.tiltaksgjennomforing.domene.varsel;

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
    ENDRET("Avtale endret");

    private final String tekst;
}
