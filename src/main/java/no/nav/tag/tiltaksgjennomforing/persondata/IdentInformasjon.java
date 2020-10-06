package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class IdentInformasjon {
    // IdentInformasjon identInformasjon;
    // private static final IdentInformasjon TOM_IDENT = new IdentInformasjon("", "", false)
    private final String ident;
    private final String gruppe;
    private final boolean historisk;
}
