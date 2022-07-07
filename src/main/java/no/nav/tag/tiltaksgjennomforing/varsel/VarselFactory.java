package no.nav.tag.tiltaksgjennomforing.varsel;

import java.util.List;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;

public class VarselFactory {
    private final Avtale avtale;
    private final Avtalerolle utførtAv;
    private final HendelseType hendelseType;

    public VarselFactory(Avtale avtale, Avtalerolle utførtAv, HendelseType hendelseType) {
        this.avtale = avtale;
        this.hendelseType = hendelseType;
        this.utførtAv = utførtAv;
    }

    public Varsel deltaker() {
        return Varsel.nyttVarsel(avtale.getDeltakerFnr(), utførtAv != Avtalerolle.DELTAKER, avtale, Avtalerolle.DELTAKER, utførtAv, hendelseType, avtale.getId());
    }

    public Varsel arbeidsgiver() {
        return Varsel.nyttVarsel(avtale.getBedriftNr(), utførtAv != Avtalerolle.ARBEIDSGIVER, avtale, Avtalerolle.ARBEIDSGIVER, utførtAv, hendelseType, avtale.getId());
    }

    public Varsel veileder() {
        return Varsel.nyttVarsel(avtale.getVeilederNavIdent(), utførtAv != Avtalerolle.VEILEDER, avtale, Avtalerolle.VEILEDER, utførtAv, hendelseType, avtale.getId());
    }

    public Varsel mentor() {
        return Varsel.nyttVarsel(new Fnr(avtale.getMentorFnr()), utførtAv != Avtalerolle.MENTOR, avtale, Avtalerolle.MENTOR, utførtAv, hendelseType, avtale.getId());
    }

    public List<Varsel> alleParter() {
        return switch (avtale.getTiltakstype()){
            case MENTOR ->  List.of(deltaker(), arbeidsgiver(), veileder(), mentor());
            default ->  List.of(deltaker(), arbeidsgiver(), veileder());
        };
    }
}
