package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.Value;

@Value
public class Oppfølgingsstatus {
    Formidlingsgruppe formidlingsgruppe;
    Kvalifiseringsgruppe kvalifiseringsgruppe;
    String oppfolgingsenhet;
    Innsatsgruppe innsatsgruppe;
}
