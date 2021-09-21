package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.Value;

@Value
public class NyNotifikasjon {
    private final String merkelapp;
    private final String tekst;
    private final String lenke;
}