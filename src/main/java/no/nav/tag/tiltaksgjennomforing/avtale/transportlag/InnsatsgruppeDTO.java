package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;

import java.util.Optional;

public record InnsatsgruppeDTO(
    Innsatsgruppe type,
    boolean erGyldigForTiltakstype
) {
    public static InnsatsgruppeDTO map(Avtale avtale) {
        return Optional.ofNullable(avtale.getInnsatsgruppe())
            .map(innsatsgruppe -> new InnsatsgruppeDTO(
                innsatsgruppe,
                innsatsgruppe.erGyldig(avtale.getTiltakstype())
            ))
            .orElse(new InnsatsgruppeDTO(null, false));
    }
}
