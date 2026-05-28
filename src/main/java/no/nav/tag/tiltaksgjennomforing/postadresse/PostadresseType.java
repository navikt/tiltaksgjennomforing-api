package no.nav.tag.tiltaksgjennomforing.postadresse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PostadresseType {

	NORSKPOSTADRESSE("NorskPostadresse"),
	UTENLANDSKPOSTADRESSE("UtenlandskPostadresse");

	@JsonValue
	private final String navn;

	@JsonCreator
	public static PostadresseType fraNavn(String navn) {
		return Arrays.stream(values())
			.filter(type -> type.navn.equals(navn) || type.name().equals(navn))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Ukjent postadressetype: " + navn));
	}
}
