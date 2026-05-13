package no.nav.tag.tiltaksgjennomforing.postadresse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostadresseType {

	NORSKPOSTADRESSE("NorskPostadresse"),
	UTENLANDSKPOSTADRESSE("UtenlandskPostadresse");

	private final String navn;
}
