package no.nav.tag.tiltaksgjennomforing.arena.client.acl;

public record AktivitetArenaAclTranslationRequest(
    long arenaId,
    AktivitetArenaAclTranslationAktivitetKategori aktivitetKategori
) {}
