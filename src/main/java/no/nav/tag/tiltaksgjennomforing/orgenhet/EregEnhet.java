package no.nav.tag.tiltaksgjennomforing.orgenhet;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;

import java.time.LocalDate;

public record EregEnhet (
    String organisasjonsnummer,
    EregNavn navn,
    String type,
    OrganisasjonDetaljer organisasjonDetaljer
) {
    public boolean erAktiv() {
        return organisasjonDetaljer.opphoersdato == null || organisasjonDetaljer.opphoersdato.isAfter(LocalDate.now());
    }

    public Organisasjon konverterTilDomeneObjekt() {
        return new Organisasjon(new BedriftNr(organisasjonsnummer), navn.sammensattnavn());
    }

    public record EregNavn (
        String navnelinje1,
        String navnelinje2,
        String navnelinje3,
        String navnelinje4,
        String sammensattnavn
    ) {}

    public record OrganisasjonDetaljer (
        LocalDate opphoersdato
    ) {}
}
