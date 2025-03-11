package no.nav.tag.tiltaksgjennomforing.persondata.domene;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;

@Value
public class Adressebeskyttelse {
    public static final Adressebeskyttelse INGEN_BESKYTTELSE = new Adressebeskyttelse(Diskresjonskode.UGRADERT);

    @JsonDeserialize(using = Diskresjonskode.DiskresjonskodeDeserializer.class)
    private final Diskresjonskode gradering;
}
