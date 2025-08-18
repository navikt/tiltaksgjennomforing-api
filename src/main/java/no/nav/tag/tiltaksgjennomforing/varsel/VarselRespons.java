package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;

import java.time.Instant;
import java.util.UUID;

public record VarselRespons(
        UUID id,
        boolean lest,
        String tekst,
        HendelseType hendelseType,
        boolean bjelle,
        UUID avtaleId,
        Instant tidspunkt,
        String utførtAv
) {
    public VarselRespons(Varsel varsel, Avtalerolle innloggetPart) {
        this(
                varsel.getId(),
                varsel.isLest(),
                varsel.getTekst(),
                varsel.getHendelseType(),
                varsel.isBjelle(),
                varsel.getAvtaleId(),
                varsel.getTidspunkt(),
                hentUtførtAv(varsel, innloggetPart)
        );
    }

    private static String hentUtførtAv(Varsel varsel, Avtalerolle innloggetPart) {
        if (innloggetPart.erInternBruker() && varsel.getUtførtAvRolle().erInternBruker()) {
            return varsel.getUtførtAvIdentifikator().asString();
        }
        return varsel.getUtførtAvRolle().name();
    }
}
