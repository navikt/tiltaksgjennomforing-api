package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;

@Value
public class Adressebeskyttelse {
    public static final Adressebeskyttelse INGEN_BESKYTTELSE = new Adressebeskyttelse(Diskresjonskode.UGRADERT);
    private final Diskresjonskode gradering;
}
