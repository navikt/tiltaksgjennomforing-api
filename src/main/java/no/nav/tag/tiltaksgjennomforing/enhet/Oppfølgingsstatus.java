package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.Value;

@Value
public class Oppfølgingsstatus {
    String oppfolgingsenhet;
    String oppfolgingsenhetNavn;
    Innsatsgruppe innsatsgruppe;
}
