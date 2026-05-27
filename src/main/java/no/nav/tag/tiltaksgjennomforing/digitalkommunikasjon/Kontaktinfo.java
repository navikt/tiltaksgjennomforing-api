package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
* @see <a href="https://docs.digdir.no/docs/Kontaktregisteret/krr_attributter#kodeverk-for-varslingsstatus">Attributter i KRR</a>
*
* Hvis kanVarsles er true betyr at Person har ikke utgått kontaktinformasjon og er ikke reservert.
* Hvis kanVarsles er false betyr det at Person har utgått kontaktinformasjon, ELLER er reservert, ELLER er slettet / finnes ikke i registeret.
* */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Kontaktinfo(Boolean reservert, Boolean kanVarsles) {

	public boolean erReservertForDigitalKontakt() {
		return Boolean.FALSE.equals(kanVarsles) && Boolean.TRUE.equals(reservert);
	}
}
