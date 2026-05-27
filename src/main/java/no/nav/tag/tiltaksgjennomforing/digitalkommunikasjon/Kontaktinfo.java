package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
* @see <a href="https://docs.digdir.no/docs/Kontaktregisteret/krr_attributter#kodeverk-for-varslingsstatus">Attributter i KRR</a>
* */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Kontaktinfo(Boolean reservert, Boolean kanVarsles) {}
