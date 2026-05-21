package no.nav.tag.tiltaksgjennomforing.postadresse;

public record Adresse(
	AdresseKildeCode adresseKilde,
	PostadresseType type,
	String adresselinje1,
	String adresselinje2,
	String adresselinje3,
	String postnummer,
	String poststed,
	String landkode,
	String land
){}
