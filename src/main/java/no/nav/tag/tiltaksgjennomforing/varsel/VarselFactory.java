package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;

import java.util.UUID;

public class VarselFactory {
    private final Avtale avtale;
    private final Avtalerolle utførtAv;
    private final VarslbarHendelseType hendelseType;

    public VarselFactory(Avtale avtale, Avtalerolle utførtAv, VarslbarHendelseType hendelseType) {
        this.avtale = avtale;
        this.hendelseType = hendelseType;
        this.utførtAv = utførtAv;
    }

    public Varsel deltaker(boolean bjelle) {
        return Varsel.nyttVarsel(avtale.getDeltakerFnr(), bjelle, avtale, Avtalerolle.DELTAKER, utførtAv, hendelseType, avtale.getId());
    }

    public Varsel arbeidsgiver(boolean bjelle) {
        return Varsel.nyttVarsel(avtale.getBedriftNr(), bjelle, avtale, Avtalerolle.ARBEIDSGIVER, utførtAv, hendelseType, avtale.getId());
    }

    public Varsel veileder(boolean bjelle) {
        return Varsel.nyttVarsel(avtale.getVeilederNavIdent(), bjelle, avtale, Avtalerolle.VEILEDER, utførtAv, hendelseType, avtale.getId());
    }
}
