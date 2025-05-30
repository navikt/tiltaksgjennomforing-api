package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HendelseType {
    OPPRETTET("Avtale er opprettet av veileder"),
    OPPRETTET_AV_ARENA("Avtale er opprettet av fagsystem (Arena)"),
    GODKJENT_AV_ARBEIDSGIVER("Avtale er godkjent av arbeidsgiver"),
    GODKJENT_AV_VEILEDER("Avtale er godkjent av veileder"),
    GODKJENT_AV_DELTAKER("Avtale er godkjent av deltaker"),
    SIGNERT_AV_MENTOR("Mentor har signert taushetserklæring"),
    GODKJENT_PAA_VEGNE_AV("Veileder godkjente avtalen på vegne av seg selv og deltaker"),
    GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER("Veileder godkjente avtalen på vegne av seg selv, deltaker og arbeidsgiver"),
    GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER("Veileder godkjente avtalen på vegne av seg selv og arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER("Avtalens godkjenninger er opphevet av arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_VEILEDER("Avtalens godkjenninger er opphevet av veileder"),
    DELT_MED_DELTAKER("Avtale delt med deltaker"),
    DELT_MED_ARBEIDSGIVER("Avtale delt med arbeidsgiver"),
    DELT_MED_MENTOR("Avtale delt med mentor"),
    ENDRET("Avtale endret"),
    ENDRET_AV_ARENA("Avtale synkronisert med fagsystem (Arena)"),
    AVBRUTT("Avtale avbrutt av veileder"),
    ANNULLERT("Avtale annullert"),
    LÅST_OPP("Avtale låst opp av veileder"),
    GJENOPPRETTET("Avtale gjenopprettet"),
    OPPRETTET_AV_ARBEIDSGIVER("Avtale er opprettet av arbeidsgiver"),
    NY_VEILEDER("Avtale tildelt ny veileder"),
    AVTALE_FORDELT("Avtale tildelt veileder"),
    TILSKUDDSPERIODE_AVSLATT("Tilskuddsperiode har blitt sendt i retur av "),
    TILSKUDDSPERIODE_GODKJENT("Tilskuddsperiode har blitt godkjent av beslutter"),
    AVTALE_FORKORTET("Avtale forkortet av veileder"),
    AVTALE_FORKORTET_AV_ARENA("Avtale forkortet av fagsystem (Arena)"),
    AVTALE_FORLENGET("Avtale forlenget av veileder"),
    AVTALE_FORLENGET_AV_ARENA("Avtale forlenget av fagsystem (Arena)"),
    MÅL_ENDRET("Mål endret av veileder"),
    INKLUDERINGSTILSKUDD_ENDRET("Inkluderingstilskudd endret av veileder"),
    OM_MENTOR_ENDRET("Om mentor endret av veileder"),
    TILSKUDDSBEREGNING_ENDRET("Tilskuddsberegning endret av veileder"),
    KONTAKTINFORMASJON_ENDRET("Kontaktinformasjon endret av veileder"),
    STILLINGSBESKRIVELSE_ENDRET("Stillingsbeskrivelse endret av veileder" ),
    OPPFØLGING_OG_TILRETTELEGGING_ENDRET("Oppfølging og tilrettelegging endret av veileder"),
    AVTALE_INNGÅTT("Avtale godkjent av NAV"),
    REFUSJON_KLAR("Refusjon klar"),
    REFUSJON_KLAR_REVARSEL("Refusjon klar, revarsel"),
    REFUSJON_FRIST_FORLENGET("Frist for refusjon forlenget"),
    REFUSJON_KORRIGERT("Refusjon korrigert"),
    VARSLER_SETT("Varsler lest"),
    AVTALE_SLETTET("Avtale slettet av veileder"),
    GODKJENT_FOR_ETTERREGISTRERING("Avtale er godkjent for etterregistrering"),
    FJERNET_ETTERREGISTRERING("Fjernet etterregistrering på avtale"),
    STATUSENDRING("Statusendring"),
    DELTAKERS_GODKJENNING_OPPHEVET_AV_VEILEDER("Deltakers godkjenning opphevet av veileder"),
    DELTAKERS_GODKJENNING_OPPHEVET_AV_ARBEIDSGIVER("Deltakers godkjenning opphevet av arbeidsgiver"),
    ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER("Arbeidsgivers godkjenning opphevet av veileder"),
    UTLOPER_OM_1_UKE("Avtale vil automatisk slettes om 1 uke dersom det ikke foretas noen endringer"),
    UTLOPER_OM_24_TIMER("Avtale vil automatisk slettes i dag dersom det ikke foretas noen endringer innen kl 23:59"),
    OPPFØLGING_AV_TILTAK_KREVES("Oppfølging av tiltaket kreves"),
    OPPFØLGING_AV_TILTAK_UTFØRT("Oppfølging av tiltaket utført"),
    PATCH("Patching av avtalehendelse"),
    OPPDATERTE_AVTALEKRAV("Avtalekrav for arbeidsgiver er oppdatert");


    private final String tekst;
}
