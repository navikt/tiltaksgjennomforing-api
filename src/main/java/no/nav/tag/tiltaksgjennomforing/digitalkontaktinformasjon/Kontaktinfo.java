package no.nav.tag.tiltaksgjennomforing.digitalkontaktinformasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;

/*
* @see <a href="https://docs.digdir.no/docs/Kontaktregisteret/krr_attributter#kodeverk-for-varslingsstatus">Attributter i KRR</a>
*
* Hvis kanVarsles er true betyr at Person har ikke utgått kontaktinformasjon og er ikke reservert.
* Hvis kanVarsles er false betyr det at Person har utgått kontaktinformasjon, ELLER er reservert, ELLER er slettet / finnes ikke i registeret.
* */
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public record Kontaktinfo(Boolean reservert, Boolean kanVarsles) {

	public boolean erReservertForDigitalKontakt() {
		log.info("Kontaktinfo: reservert={}, kanVarsles={}", reservert, kanVarsles);
		return Boolean.FALSE.equals(kanVarsles) && Boolean.TRUE.equals(reservert);
	}
}
