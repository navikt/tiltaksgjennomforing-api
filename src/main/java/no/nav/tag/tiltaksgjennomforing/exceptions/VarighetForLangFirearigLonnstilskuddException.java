package no.nav.tag.tiltaksgjennomforing.exceptions;

import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;

import java.util.Map;
import java.util.Optional;

public class VarighetForLangFirearigLonnstilskuddException extends FeilkodeException {
    private static final Map<Stillingstype, Feilkode>  FEILKODE_MAP = Map.of(
        Stillingstype.FAST, Feilkode.VARIGHET_FOR_LANG_FIREARIG_LONNSTILSKUDD_4_AAR,
        Stillingstype.MIDLERTIDIG, Feilkode.VARIGHET_FOR_LANG_FIREARIG_LONNSTILSKUDD_2_AAR
    );

    public VarighetForLangFirearigLonnstilskuddException(Stillingstype stillingstype) {
        super(
            Optional.ofNullable(stillingstype)
                .map(FEILKODE_MAP::get)
                .orElse(Feilkode.VARIGHET_FOR_LANG_FIREARIG_LONNSTILSKUDD_4_AAR)
        );
    }
}
