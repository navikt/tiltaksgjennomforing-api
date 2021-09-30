package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request;

import lombok.Value;

@Value
public class Variables {
    String eksternId;
    String virksomhetsnummer;
    String lenke;
    String serviceCode;
    String serviceEdition;
    String merkelapp;
    String tekst;
}
