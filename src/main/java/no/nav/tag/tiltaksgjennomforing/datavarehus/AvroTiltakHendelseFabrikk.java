package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@UtilityClass
public class AvroTiltakHendelseFabrikk {
    public static AvroTiltakHendelse konstruer(Avtale avtale, LocalDateTime tidspunkt, UUID meldingId, DvhHendelseType hendelseType) {
        AvroTiltakHendelse hendelse = new AvroTiltakHendelse();
        hendelse.setMeldingId(meldingId.toString());
        hendelse.setTidspunkt(toInstant(tidspunkt));
        hendelse.setAvtaleId(avtale.getId().toString());
        hendelse.setAvtaleInnholdId(avtale.gjeldendeInnhold().getId().toString());
        hendelse.setTiltakstype(TiltakType.valueOf(avtale.getTiltakstype().name()));
        hendelse.setTiltakskodeArena(avtale.getTiltakstype().getTiltakskodeArena() != null ? TiltakKodeArena.valueOf(avtale.getTiltakstype().getTiltakskodeArena()) : null);
        hendelse.setHendelseType(hendelseType.name());
        hendelse.setTiltakStatus(avtale.statusSomEnum().name());
        hendelse.setDeltakerFnr(avtale.getDeltakerFnr().asString());
        hendelse.setBedriftNr(avtale.getBedriftNr().asString());
        hendelse.setHarFamilietilknytning(avtale.getHarFamilietilknytning());
        hendelse.setVeilederNavIdent(avtale.getVeilederNavIdent().asString());
        hendelse.setStartDato(avtale.getStartDato());
        hendelse.setSluttDato(avtale.getSluttDato());
        hendelse.setStillingprosent(avtale.getStillingprosent());
        hendelse.setAntallDagerPerUke(avtale.getAntallDagerPerUke());
        hendelse.setStillingstittel(avtale.getStillingstittel());
        hendelse.setStillingstype(avtale.getStillingstype() != null ? StillingType.valueOf(avtale.getStillingstype().name()) : null);
        hendelse.setStillingStyrk08(avtale.getStillingStyrk08());
        hendelse.setStillingKonseptId(avtale.getStillingKonseptId());
        hendelse.setLonnstilskuddProsent(avtale.getLonnstilskuddProsent());
        hendelse.setManedslonn(avtale.getManedslonn());
        hendelse.setFeriepengesats(avtale.getFeriepengesats() != null ? avtale.getFeriepengesats().floatValue() : null);
        hendelse.setFeriepengerBelop(avtale.getFeriepengerBelop());
        hendelse.setArbeidsgiveravgift(avtale.getArbeidsgiveravgift() != null ? avtale.getArbeidsgiveravgift().floatValue() : null);
        hendelse.setArbeidsgiveravgiftBelop(avtale.getFeriepengerBelop());
        hendelse.setOtpSats(avtale.getOtpSats() != null ? avtale.getOtpSats().floatValue() : null);
        hendelse.setOtpBelop(avtale.getOtpBelop());
        hendelse.setSumLonnsutgifter(avtale.getSumLonnsutgifter());
        hendelse.setSumLonnstilskudd(avtale.getSumLonnstilskudd());
        hendelse.setSumLonnstilskuddRedusert(avtale.getSumLønnstilskuddRedusert());
        hendelse.setDatoForRedusertProsent(avtale.getDatoForRedusertProsent());
        hendelse.setGodkjentPaVegneAv(avtale.isGodkjentPaVegneAv());
        hendelse.setIkkeBankId(avtale.getGodkjentPaVegneGrunn() != null && avtale.getGodkjentPaVegneGrunn().isIkkeBankId());
        hendelse.setReservert(avtale.getGodkjentPaVegneGrunn() != null && avtale.getGodkjentPaVegneGrunn().isReservert());
        hendelse.setDigitalKompetanse(avtale.getGodkjentPaVegneGrunn() != null && avtale.getGodkjentPaVegneGrunn().isDigitalKompetanse());
        hendelse.setGodkjentAvDeltaker(toInstant(avtale.getGodkjentAvDeltaker()));
        hendelse.setGodkjentAvArbeidsgiver(toInstant(avtale.getGodkjentAvArbeidsgiver()));
        hendelse.setGodkjentAvVeileder(toInstant(avtale.getGodkjentAvVeileder()));
        hendelse.setUtfortAv(avtale.getGodkjentAvNavIdent().asString());
        hendelse.setEnhetOppfolging(avtale.getEnhetOppfolging());
        hendelse.setEnhetGeografisk(avtale.getEnhetGeografisk());
        hendelse.setOpprettetAvArbeidsgiver(avtale.isOpprettetAvArbeidsgiver());
        hendelse.setAnnullertTidspunkt(avtale.getAnnullertTidspunkt());
        hendelse.setAnnullertGrunn(avtale.getAnnullertGrunn());
        return hendelse;
    }

    private static Instant toInstant(LocalDateTime tidspunkt) {
        return tidspunkt.atZone(ZoneId.systemDefault()).toInstant();
    }
}
