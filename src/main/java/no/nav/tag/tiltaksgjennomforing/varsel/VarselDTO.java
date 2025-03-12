package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;

import java.time.LocalDateTime;
import java.util.UUID;

public record VarselDTO(
        UUID id,
        boolean lest,
        String tekst,
        HendelseType hendelseType,
        boolean bjelle,
        UUID avtaleId,
        LocalDateTime tidspunkt,
        String utførtAv
) {
    public VarselDTO(Varsel varsel, Avtalerolle innloggetPart) {
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
        if (innloggetPart.erInternBruker() && varsel.getUtførtAv().erInternBruker()) {
            return varsel.getIdentifikator().asString();
        }
        return varsel.getUtførtAv().name();
    }
}
