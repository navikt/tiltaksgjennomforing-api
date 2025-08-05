package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class AvroTiltakHendelseFabrikk {
    public static AvroTiltakHendelse konstruer(Avtale avtale, DvhHendelseType hendelseType, String utførtAv) {
        return konstruer(avtale, hendelseType, utførtAv, null);
    }

    public static AvroTiltakHendelse konstruer(Avtale avtale, DvhHendelseType hendelseType, String utførtAv, ForkortetGrunn forkortetGrunn) {
        AvroTiltakHendelse hendelse = new AvroTiltakHendelse();
        hendelse.setMeldingId(UUID.randomUUID().toString());
        hendelse.setTidspunkt(Now.instant());
        hendelse.setAvtaleId(avtale.getId().toString());
        hendelse.setAvtaleInnholdId(avtale.getGjeldendeInnhold().getId().toString());
        hendelse.setTiltakstype(TiltakType.valueOf(avtale.getTiltakstype().name()));
        hendelse.setTiltakskodeArena(avtale.getTiltakstype().getTiltakskodeArena() != null ? TiltakKodeArena.valueOf(avtale.getTiltakstype().getTiltakskodeArena()) : null);
        hendelse.setHendelseType(hendelseType.name());
        hendelse.setTiltakStatus(avtale.getStatus().name());
        hendelse.setDeltakerFnr(avtale.getDeltakerFnr().asString());
        hendelse.setBedriftNr(avtale.getBedriftNr().asString());
        hendelse.setVeilederNavIdent(avtale.getVeilederNavIdent().asString());
        hendelse.setHarFamilietilknytning(avtale.getGjeldendeInnhold().getHarFamilietilknytning());
        hendelse.setStartDato(avtale.getGjeldendeInnhold().getStartDato());
        hendelse.setSluttDato(avtale.getGjeldendeInnhold().getSluttDato());
        hendelse.setStillingprosent(avtale.getGjeldendeInnhold().getStillingprosent() != null ?  avtale.getGjeldendeInnhold().getStillingprosent().floatValue() : null);
        hendelse.setAntallDagerPerUke(avtale.getGjeldendeInnhold().getAntallDagerPerUke() != null ? avtale.getGjeldendeInnhold().getAntallDagerPerUke().floatValue() : null);
        hendelse.setStillingstittel(avtale.getGjeldendeInnhold().getStillingstittel());
        hendelse.setStillingstype(avtale.getGjeldendeInnhold().getStillingstype() != null ? StillingType.valueOf(avtale.getGjeldendeInnhold().getStillingstype().name()) : null);
        hendelse.setStillingStyrk08(avtale.getGjeldendeInnhold().getStillingStyrk08());
        hendelse.setStillingKonseptId(avtale.getGjeldendeInnhold().getStillingKonseptId());
        hendelse.setLonnstilskuddProsent(avtale.getGjeldendeInnhold().getLonnstilskuddProsent());
        hendelse.setManedslonn(avtale.getGjeldendeInnhold().getManedslonn());
        hendelse.setFeriepengesats(avtale.getGjeldendeInnhold().getFeriepengesats() != null ? avtale.getGjeldendeInnhold().getFeriepengesats().floatValue() : null);
        hendelse.setFeriepengerBelop(avtale.getGjeldendeInnhold().getFeriepengerBelop());
        hendelse.setArbeidsgiveravgift(avtale.getGjeldendeInnhold().getArbeidsgiveravgift() != null ? avtale.getGjeldendeInnhold().getArbeidsgiveravgift().floatValue() : null);
        hendelse.setArbeidsgiveravgiftBelop(avtale.getGjeldendeInnhold().getFeriepengerBelop());
        hendelse.setOtpSats(avtale.getGjeldendeInnhold().getOtpSats() != null ? avtale.getGjeldendeInnhold().getOtpSats().floatValue() : null);
        hendelse.setOtpBelop(avtale.getGjeldendeInnhold().getOtpBelop());
        hendelse.setSumLonnsutgifter(avtale.getGjeldendeInnhold().getSumLonnsutgifter());
        hendelse.setSumLonnstilskudd(avtale.getGjeldendeInnhold().getSumLonnstilskudd());
        hendelse.setSumLonnstilskuddRedusert(avtale.getGjeldendeInnhold().getSumLønnstilskuddRedusert());
        hendelse.setDatoForRedusertProsent(avtale.getGjeldendeInnhold().getDatoForRedusertProsent());
        hendelse.setGodkjentPaVegneAv(avtale.getGjeldendeInnhold().isGodkjentPaVegneAv());
        hendelse.setIkkeBankId(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isIkkeBankId());
        hendelse.setReservert(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isReservert());
        hendelse.setDigitalKompetanse(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isDigitalKompetanse());
        hendelse.setArenaMigreringDeltaker(avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn() != null && avtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isArenaMigreringDeltaker());
        hendelse.setGodkjentAvDeltaker(avtale.getGjeldendeInnhold().getGodkjentAvDeltaker());
        hendelse.setGodkjentAvArbeidsgiver(avtale.getGjeldendeInnhold().getGodkjentAvArbeidsgiver());
        hendelse.setGodkjentAvVeileder(avtale.getGjeldendeInnhold().getGodkjentAvVeileder());
        hendelse.setGodkjentAvBeslutter(avtale.getGjeldendeInnhold().getGodkjentAvBeslutter());
        hendelse.setAvtaleInngaatt(avtale.getGjeldendeInnhold().getAvtaleInngått());
        hendelse.setUtfortAv(utførtAv);
        hendelse.setEnhetOppfolging(avtale.getEnhetOppfolging());
        hendelse.setEnhetGeografisk(avtale.getEnhetGeografisk());
        hendelse.setOpprettetAvArbeidsgiver(Avtaleopphav.ARBEIDSGIVER.equals(avtale.getOpphav()));
        hendelse.setAnnullertTidspunkt(avtale.getAnnullertTidspunkt());
        hendelse.setAnnullertGrunn(avtale.getAnnullertGrunn());
        hendelse.setMaster(erMaster(avtale));
        hendelse.setForkortetGrunn(Optional.ofNullable(forkortetGrunn).flatMap(ForkortetGrunn::utled).orElse(null));
        return hendelse;
    }

    private Boolean erMaster(Avtale avtale) {
        if (avtale.getTiltakstype() == Tiltakstype.SOMMERJOBB || avtale.getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || avtale.getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
