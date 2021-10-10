package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request;

import lombok.Value;

@Value
public class VariablesMedAvtale {
    String id;
    String eksternId;
    String virksomhetsnummer;
    String lenke;
    String serviceCode;
    String serviceEdition;
    String merkelapp;
    String tekst;
}
