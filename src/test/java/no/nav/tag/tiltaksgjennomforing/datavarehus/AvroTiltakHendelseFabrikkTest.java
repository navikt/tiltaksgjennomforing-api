package no.nav.tag.tiltaksgjennomforing.datavarehus;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvroTiltakHendelseFabrikkTest {

    private static final Instant FAST_TIDSPUNKT = Instant.parse("2024-01-01T10:00:00Z");

    private static AvroTiltakHendelse enHendelse(String hendelseType) {
        AvroTiltakHendelse hendelse = new AvroTiltakHendelse();
        hendelse.setMeldingId(UUID.randomUUID().toString());
        hendelse.setTidspunkt(FAST_TIDSPUNKT);
        hendelse.setHendelseType(hendelseType);
        hendelse.setUtfortAv("Z123456");
        hendelse.setAvtaleId("b1e5c0a0-0000-0000-0000-000000000001");
        hendelse.setAvtaleInnholdId("b1e5c0a0-0000-0000-0000-000000000002");
        hendelse.setTiltakstype(TiltakType.ARBEIDSTRENING);
        hendelse.setTiltakskodeArena(null);
        hendelse.setTiltakStatus("GJENNOMFØRES");
        hendelse.setDeltakerFnr("12345678910");
        hendelse.setBedriftNr("999999999");
        hendelse.setHarFamilietilknytning(false);
        hendelse.setVeilederNavIdent("Z123456");
        hendelse.setStartDato(LocalDate.of(2024, 1, 1));
        hendelse.setSluttDato(LocalDate.of(2024, 12, 31));
        hendelse.setStillingprosent(100.0f);
        hendelse.setAntallDagerPerUke(5.0f);
        hendelse.setArbeidstreningsMaal(List.of());
        hendelse.setStillingstype(null);
        hendelse.setStillingstittel(null);
        hendelse.setStillingStyrk08(null);
        hendelse.setStillingKonseptId(null);
        hendelse.setLonnstilskuddProsent(null);
        hendelse.setManedslonn(null);
        hendelse.setFeriepengesats(null);
        hendelse.setFeriepengerBelop(null);
        hendelse.setArbeidsgiveravgift(null);
        hendelse.setArbeidsgiveravgiftBelop(null);
        hendelse.setOtpSats(null);
        hendelse.setOtpBelop(null);
        hendelse.setSumLonnsutgifter(null);
        hendelse.setSumLonnstilskudd(null);
        hendelse.setGodkjentPaVegneAv(false);
        hendelse.setIkkeBankId(false);
        hendelse.setReservert(false);
        hendelse.setDigitalKompetanse(false);
        hendelse.setArenaMigreringDeltaker(false);
        hendelse.setGodkjentAvDeltaker(FAST_TIDSPUNKT);
        hendelse.setGodkjentAvArbeidsgiver(FAST_TIDSPUNKT);
        hendelse.setGodkjentAvVeileder(FAST_TIDSPUNKT);
        hendelse.setGodkjentAvBeslutter(null);
        hendelse.setAvtaleInngaatt(FAST_TIDSPUNKT);
        hendelse.setEnhetOppfolging(null);
        hendelse.setEnhetGeografisk(null);
        hendelse.setOpprettetAvArbeidsgiver(false);
        hendelse.setAnnullertTidspunkt(null);
        hendelse.setAnnullertGrunn(null);
        hendelse.setMaster(true);
        hendelse.setForkortetGrunn(null);
        hendelse.setAvtaleNr(null);
        hendelse.setMentorTimelonn(null);
        hendelse.setMentorAntallTimer(null);
        hendelse.setLonnstilskuddFormaal(null);
        hendelse.setInkluderingstilskuddsutgift(List.of());
        hendelse.setTilskuddstrinn(List.of());
        return hendelse;
    }

    @Test
    void beregnNøkkel_girSammeNøkkelForSammeInnhold() {
        AvroTiltakHendelse hendelse1 = enHendelse(DvhHendelseType.ENDRET.name());
        AvroTiltakHendelse hendelse2 = enHendelse(DvhHendelseType.ENDRET.name());

        assertThat(hendelse1.getMeldingId()).isNotEqualTo(hendelse2.getMeldingId());
        assertThat(AvroTiltakHendelseFabrikk.beregnNøkkel(hendelse1))
                .isEqualTo(AvroTiltakHendelseFabrikk.beregnNøkkel(hendelse2));
    }

    @Test
    void beregnNøkkel_girUlikNøkkelForUlikHendelseType() {
        AvroTiltakHendelse endret = enHendelse(DvhHendelseType.ENDRET.name());
        AvroTiltakHendelse inngått = enHendelse(DvhHendelseType.INNGÅTT.name());

        assertThat(AvroTiltakHendelseFabrikk.beregnNøkkel(endret))
                .isNotEqualTo(AvroTiltakHendelseFabrikk.beregnNøkkel(inngått));
    }
}
