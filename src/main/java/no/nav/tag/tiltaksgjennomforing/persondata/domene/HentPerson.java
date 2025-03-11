package no.nav.tag.tiltaksgjennomforing.persondata.domene;

import java.util.List;

public record HentPerson (
    List<Adressebeskyttelse> adressebeskyttelse,
    List<Folkeregisteridentifikator> folkeregisteridentifikator,
    List<Navn> navn
) {}
