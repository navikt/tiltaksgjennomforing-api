package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VarslbarHendelseType {
    OPPRETTET("Avtale opprettet av NAV-veileder"),
    GODKJENT_AV_ARBEIDSGIVER("Avtale godkjent av arbeidsgiver"),
    GODKJENT_AV_VEILEDER("Avtale godkjent av NAV-veileder"),
    GODKJENT_AV_DELTAKER("Avtale godkjent av deltaker"),
    GODKJENT_PAA_VEGNE_AV("Avtale godkjent av NAV-veileder"),
    GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER("Avtalens godkjenninger opphevet av arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_VEILEDER("Avtalens godkjenninger opphevet av NAV-veileder"),
    SMS_VARSLING_FEILET("Varsling på SMS feilet"),
    ENDRET("Avtale endret");

    private final String tekst;
}
