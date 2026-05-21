package no.nav.tag.tiltaksgjennomforing.datavarehus;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvroTiltakHendelseFabrikkTest {

    private static final Instant FAST_TIDSPUNKT = Instant.parse("2024-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    void konstruer_melding_id_er_hash_av_alt_innhold() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();

        AvroTiltakHendelse hendelse = AvroTiltakHendelseFabrikk.konstruer(avtale, DvhHendelseType.ENDRET, "Z123456");

        // meldingId must equal beregnNokkel() on the returned message.
        // If any field is set after setMeldingId() in konstruer(), this assertion fails.
        assertThat(hendelse.getMeldingId())
                .isEqualTo(AvroTiltakHendelseFabrikk.beregnNokkel(hendelse));
    }

    @Test
    void beregn_nokkel_gir_samme_nokkel_for_samme_innhold() {
        AvroTiltakHendelse hendelse1 = enHendelse(DvhHendelseType.ENDRET.name());
        AvroTiltakHendelse hendelse2 = enHendelse(DvhHendelseType.ENDRET.name());

        assertThat(hendelse1.getMeldingId()).isNotEqualTo(hendelse2.getMeldingId());
        assertThat(AvroTiltakHendelseFabrikk.beregnNokkel(hendelse1))
                .isEqualTo(AvroTiltakHendelseFabrikk.beregnNokkel(hendelse2));
    }

    @Test
    void beregn_nokkel_gir_ulik_nokkel_for_ulik_hendelse_type() {
        AvroTiltakHendelse endret = enHendelse(DvhHendelseType.ENDRET.name());
        AvroTiltakHendelse inngått = enHendelse(DvhHendelseType.INNGÅTT.name());

        assertThat(AvroTiltakHendelseFabrikk.beregnNokkel(endret))
                .isNotEqualTo(AvroTiltakHendelseFabrikk.beregnNokkel(inngått));
    }

    @Test
    void beregn_nokkel_er_uavhengig_av_tidspunkt() {
        AvroTiltakHendelse hendelse1 = enHendelse(DvhHendelseType.ENDRET.name());
        AvroTiltakHendelse hendelse2 = enHendelse(DvhHendelseType.ENDRET.name());
        hendelse2.setTidspunkt(Instant.parse("2099-12-31T23:59:59Z"));

        assertThat(AvroTiltakHendelseFabrikk.beregnNokkel(hendelse1))
                .isEqualTo(AvroTiltakHendelseFabrikk.beregnNokkel(hendelse2));
    }

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
}
