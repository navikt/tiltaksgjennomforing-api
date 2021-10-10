package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request;

import lombok.Value;

@Value
public class ArbeidsgiverMutationRequestMedAvtale {
        String query;
        VariablesMedAvtale variablesMedAvtale;
}
