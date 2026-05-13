package no.nav.tag.tiltaksgjennomforing.postadresse;

import lombok.Builder;
import java.util.Set;
@Builder(toBuilder = true)
public record PostadresseRequest (String ident,
								  Set<String> filtrerAdressebeskyttelse){}
