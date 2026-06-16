package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;

public enum VersjonInnhold {
    MENTOR_BEREGNING,
    LONNSTILSKUDD_FORMAAL,
    OPPRINNELIG;

    public static VersjonInnhold parse(AvtaleInnhold dbEntitet) {
        Avtale avtale = dbEntitet.getAvtale();

        return switch (avtale.getTiltakstype()) {
            case MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, FIREARIG_LONNSTILSKUDD -> parseLonnstilskudd(dbEntitet);
            case MENTOR -> parseMentor(dbEntitet);
            case ARBEIDSTRENING, INKLUDERINGSTILSKUDD, SOMMERJOBB, VTAO -> OPPRINNELIG;
        };
    }

    private static VersjonInnhold parseLonnstilskudd(AvtaleInnhold dbEntitet) {
        if (!dbEntitet.getAvtale().erAvtaleInngått() || dbEntitet.getLonnstilskuddFormaal() != null) {
            return LONNSTILSKUDD_FORMAAL;
        }
        return OPPRINNELIG;
    }

    private static VersjonInnhold parseMentor(AvtaleInnhold dbEntitet) {
        if (!dbEntitet.getAvtale().erAvtaleInngått() || dbEntitet.getMentorValgtLonnstype() != null) {
            return MENTOR_BEREGNING;
        }
        return OPPRINNELIG;
    }

}
