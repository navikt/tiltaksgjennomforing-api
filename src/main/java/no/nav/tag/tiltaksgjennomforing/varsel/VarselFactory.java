package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAv;

import java.util.List;

public class VarselFactory {
    private final Avtale avtale;
    private final AvtaleHendelseUtførtAv utfortAv;
    private final HendelseType hendelseType;
    private final TilskuddPeriode tilskuddPeriode;

    public VarselFactory(Avtale avtale, AvtaleHendelseUtførtAv utfortAv, HendelseType hendelseType) {
        this(avtale, null, utfortAv, hendelseType);
    }

    public VarselFactory(Avtale avtale, TilskuddPeriode tilskuddPeriode, AvtaleHendelseUtførtAv utfortAv, HendelseType hendelseType) {
        this.avtale = avtale;
        this.hendelseType = hendelseType;
        this.utfortAv = utfortAv;
        this.tilskuddPeriode = tilskuddPeriode;
    }

    public Varsel deltaker() {
        return Varsel.nyttVarsel(avtale.getDeltakerFnr(), utfortAv.rolle() != AvtaleHendelseUtførtAv.Rolle.DELTAKER, avtale, Avtalerolle.DELTAKER, utfortAv, hendelseType, tilskuddPeriode);
    }

    public Varsel arbeidsgiver() {
        return Varsel.nyttVarsel(avtale.getBedriftNr(), utfortAv.rolle() != AvtaleHendelseUtførtAv.Rolle.ARBEIDSGIVER, avtale, Avtalerolle.ARBEIDSGIVER, utfortAv, hendelseType, tilskuddPeriode);
    }

    //TODO: Hent IDENTEN til beslutter her og ikke bare veileder
    public Varsel veileder() {
        return Varsel.nyttVarsel(avtale.getVeilederNavIdent(), utfortAv.rolle() != AvtaleHendelseUtførtAv.Rolle.VEILEDER, avtale, Avtalerolle.VEILEDER, utfortAv, hendelseType, tilskuddPeriode);
    }

    public Varsel mentor() {
        return Varsel.nyttVarsel(avtale.getMentorFnr(), utfortAv.rolle() != AvtaleHendelseUtførtAv.Rolle.MENTOR, avtale, Avtalerolle.MENTOR, utfortAv, hendelseType, tilskuddPeriode);
    }

    public List<Varsel> alleParter() {
        if (avtale.getTiltakstype() == Tiltakstype.MENTOR) {
            return List.of(deltaker(), arbeidsgiver(), veileder(), mentor());
        }
        return List.of(deltaker(), arbeidsgiver(), veileder());
    }
}
