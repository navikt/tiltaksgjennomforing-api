package no.nav.tag.tiltaksgjennomforing.postadresse;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostadresseType {

	NORSKPOSTADRESSE("NorskPostadresse"),
	UTENLANDSKPOSTADRESSE("UtenlandskPostadresse");

	@JsonValue
	private final String navn;
}
