package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import org.assertj.core.groups.Tuple;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class LagSmsVarselFraVarslbarHendelseTest {
    private static Tuple deltaker(Avtale avtale) {
        return tuple(
                avtale.getDeltakerTlf(),
                avtale.getDeltakerFnr(),
                "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing");
    }

    private static Tuple arbeidsgiver(Avtale avtale) {
        return tuple(
                avtale.getArbeidsgiverTlf(),
                avtale.getBedriftNr(),
                "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing");
    }

    private static Tuple veileder(Avtale avtale) {
        return tuple(
                avtale.getVeilederTlf(),
                SmsVarselFactory.NAV_ORGNR,
                "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing");
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENT_AV_DELTAKER() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENT_AV_DELTAKER), new GamleVerdier());
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(veileder(avtale))
                .doesNotContain(deltaker(avtale), arbeidsgiver(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENT_AV_ARBEIDSGIVER() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER), new GamleVerdier());
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(veileder(avtale))
                .doesNotContain(deltaker(avtale), arbeidsgiver(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENT_AV_VEILEDER() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENT_AV_VEILEDER), new GamleVerdier());
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(deltaker(avtale), arbeidsgiver(avtale))
                .doesNotContain(veileder(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER_deltaker_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        GamleVerdier gamleVerdier = new GamleVerdier();
        gamleVerdier.setGodkjentAvDeltaker(true);
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER), gamleVerdier);
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(deltaker(avtale), veileder(avtale))
                .doesNotContain(arbeidsgiver(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER_deltaker_har_ikke_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER), new GamleVerdier());
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(veileder(avtale))
                .doesNotContain(deltaker(avtale), arbeidsgiver(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENNINGER_OPPHEVET_AV_VEILEDER_ag_har_godkjent_deltaker_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER), new GamleVerdier(true, true));
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(deltaker(avtale), arbeidsgiver(avtale))
                .doesNotContain(veileder(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENNINGER_OPPHEVET_AV_VEILEDER_ag_har_ikke_godkjent_deltaker_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER), new GamleVerdier(true, false));
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(deltaker(avtale))
                .doesNotContain(arbeidsgiver(avtale), veileder(avtale));
    }

    @Test
    public void nyHendelse__skal_opprette_riktige_sms_varsler_GODKJENNINGER_OPPHEVET_AV_VEILEDER_ag_har_godkjent_deltaker_har_ikke_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER), new GamleVerdier(false, true));
        assertThat(smsVarsler).extracting("telefonnummer", "identifikator", "meldingstekst")
                .contains(arbeidsgiver(avtale))
                .doesNotContain(deltaker(avtale), veileder(avtale));
    }

    @Test
    public void nyHendelse__skal_ikke_opprette_sms_varsler_ENDRET() {
        Avtale avtale = TestData.enAvtale();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.ENDRET), new GamleVerdier());
        assertThat(smsVarsler).isEmpty();
    }

    @Test
    public void nyHendelse__skal_ikke_opprette_sms_varsler_OPPRETTET() {
        Avtale avtale = TestData.enAvtale();
        List<SmsVarsel> smsVarsler = LagSmsVarselFraVarslbarHendelse.lagSmsVarsler(avtale, VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.OPPRETTET), new GamleVerdier());
        assertThat(smsVarsler).isEmpty();
    }
}