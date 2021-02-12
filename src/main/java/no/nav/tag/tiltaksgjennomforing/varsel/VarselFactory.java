package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;

public class VarselFactory {
    private final Avtale avtale;
    private final Avtalerolle utførtAv;
    private final VarslbarHendelseType hendelseType;

    public VarselFactory(Avtale avtale, Avtalerolle utførtAv, VarslbarHendelseType hendelseType) {
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
}
