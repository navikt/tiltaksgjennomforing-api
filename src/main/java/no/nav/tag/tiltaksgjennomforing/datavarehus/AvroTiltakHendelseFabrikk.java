package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@UtilityClass
public class AvroTiltakHendelseFabrikk {
    public static AvroTiltakHendelse konstruer(Avtale avtale, LocalDateTime tidspunkt, UUID meldingId, DvhHendelseType hendelseType) {
        AvroTiltakHendelse hendelse = new AvroTiltakHendelse();
        hendelse.setMeldingId(meldingId.toString());
        hendelse.setTidspunkt(tidspunkt.toString());
        hendelse.setAvtaleId(avtale.getId().toString());
        hendelse.setAvtaleInnholdId(avtale.gjeldendeInnhold().getId().toString());
        hendelse.setTiltakstype(avtale.getTiltakstype().name());
        hendelse.setTiltakskodeArena(avtale.getTiltakstype().getTiltakskodeArena());
        hendelse.setHendelseType(hendelseType.name());
        hendelse.setTiltakStatus(avtale.statusSomEnum().name());
        hendelse.setDeltakerFnr(avtale.getDeltakerFnr().asString());
        hendelse.setBedriftNr(avtale.getBedriftNr().asString());
        hendelse.setHarFamilietilknytning(avtale.getHarFamilietilknytning());
        hendelse.setVeilederNavIdent(avtale.getVeilederNavIdent().asString());
        hendelse.setStartDato(avtale.getStartDato().toString());
        hendelse.setSluttDato(avtale.getSluttDato().toString());
        hendelse.setStillingprosent(avtale.getStillingprosent());
        hendelse.setAntallDagerPerUke(avtale.getAntallDagerPerUke());
        hendelse.setStillingstittel(avtale.getStillingstittel());
        hendelse.setStillingstype(avtale.getStillingstype() != null ? avtale.getStillingstype().name() : null);
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
        hendelse.setDatoForRedusertProsent(avtale.getDatoForRedusertProsent() != null ? avtale.getDatoForRedusertProsent().toString() : null);
        hendelse.setGodkjentPaVegneAv(avtale.isGodkjentPaVegneAv());
        hendelse.setIkkeBankId(avtale.getGodkjentPaVegneGrunn() != null ? avtale.getGodkjentPaVegneGrunn().isIkkeBankId() : false);
        hendelse.setReservert(avtale.getGodkjentPaVegneGrunn() != null ? avtale.getGodkjentPaVegneGrunn().isReservert() : false);
        hendelse.setDigitalKompetanse(avtale.getGodkjentPaVegneGrunn() != null ? avtale.getGodkjentPaVegneGrunn().isDigitalKompetanse() : false);
        hendelse.setGodkjentAvDeltaker(avtale.getGodkjentAvDeltaker().toString());
        hendelse.setGodkjentAvArbeidsgiver(avtale.getGodkjentAvArbeidsgiver().toString());
        hendelse.setGodkjentAvVeileder(avtale.getGodkjentAvVeileder().toString());
        hendelse.setGodkjentAvNavIdent(avtale.getGodkjentAvNavIdent().asString());
        hendelse.setEnhetOppfolging(avtale.getEnhetOppfolging());
        hendelse.setEnhetGeografisk(avtale.getEnhetGeografisk());
        hendelse.setOpprettetAvArbeidsgiver(avtale.isOpprettetAvArbeidsgiver());
        hendelse.setAnnullertTidspunkt(avtale.getAnnullertTidspunkt() != null ? avtale.getAnnullertTidspunkt().atZone(ZoneId.systemDefault()).toLocalDateTime().toString() : null);
        hendelse.setAnnullertGrunn(avtale.getAnnullertGrunn());
        return hendelse;
    }
}
