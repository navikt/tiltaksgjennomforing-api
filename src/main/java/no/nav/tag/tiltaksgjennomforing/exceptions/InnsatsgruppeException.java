package no.nav.tag.tiltaksgjennomforing.exceptions;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

public class InnsatsgruppeException extends FeilkodeException {

    public InnsatsgruppeException(Feilkode feilkode) {
        super(feilkode);
    }

    public static FeilkodeException fraTiltakstype(Tiltakstype tiltakstype) {
        return switch (tiltakstype) {
            case ARBEIDSTRENING, MENTOR, INKLUDERINGSTILSKUDD -> new InnsatsgruppeException(Feilkode.INNSATSGRUPPE_IKKE_RETTIGHET);
            case MIDLERTIDIG_LONNSTILSKUDD, SOMMERJOBB -> new InnsatsgruppeException(Feilkode.INNSATSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL);
            case VARIG_LONNSTILSKUDD -> new InnsatsgruppeException(Feilkode.INNSATSGRUPPE_VARIG_LONNTILSKUDD_FEIL);
            case VTAO -> new InnsatsgruppeException(Feilkode.INNSATSGRUPPE_VTAO_FEIL);
            case FIREARIG_LONNSTILSKUDD -> new InnsatsgruppeException(Feilkode.INNSATSGRUPPE_FIREARIG_LONNTILSKUDD_FOR_UNGE_FEIL);
        };
    }

}
