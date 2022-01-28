package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.SmsProducer;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.Topics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = { "tiltaksgjennomforing.kafka.enabled=true" })
@DirtiesContext
@ActiveProfiles(Miljø.LOCAL)
@EmbeddedKafka(partitions = 1, topics = {Topics.TILTAK_SMS })
class LagSmsFraAvtaleHendelseTest {
    @Autowired
    SmsRepository smsRepository;
    @Autowired
    AvtaleRepository avtaleRepository;
    @SpyBean
    SmsProducer smsProducer;

    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";



    @Test
    void avtaleDeltMedAvtalepart__skal_opprette_sms_til_riktig_mottaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        avtale.getGjeldendeInnhold().setDeltakerTlf("42234567");
        avtale.delMedAvtalepart(Avtalerolle.ARBEIDSGIVER);
        avtaleRepository.save(avtale);
        assertSmsOpprettetOgSendt(HendelseType.DELT_MED_ARBEIDSGIVER, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), SELVBETJENINGSONE_VARSELTEKST);
        avtale.delMedAvtalepart(Avtalerolle.DELTAKER);
        avtaleRepository.save(avtale);
        assertSmsOpprettetOgSendt(HendelseType.DELT_MED_DELTAKER, avtale.getId(), avtale.getGjeldendeInnhold().getDeltakerTlf(), SELVBETJENINGSONE_VARSELTEKST);
    }

    @Test
    void avtaleGodkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Deltaker deltaker = TestData.enDeltaker(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        avtaleRepository.save(avtale);
        assertSmsOpprettetOgSendt(HendelseType.GODKJENT_AV_ARBEIDSGIVER, avtale.getId(), avtale.getGjeldendeInnhold().getVeilederTlf(), FAGSYSTEMSONE_VARSELTEKST);
        deltaker.godkjennAvtale(Instant.now(), avtale);
        avtaleRepository.save(avtale);
        assertSmsOpprettetOgSendt(HendelseType.GODKJENT_AV_DELTAKER, avtale.getId(), avtale.getGjeldendeInnhold().getVeilederTlf(), FAGSYSTEMSONE_VARSELTEKST);
    }

    @Test
    void avtaleInngått() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = new GodkjentPaVegneGrunn();
        godkjentPaVegneGrunn.setIkkeBankId(true);
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
        avtaleRepository.save(avtale);

        assertSmsOpprettetOgSendt(HendelseType.AVTALE_INNGÅTT, avtale.getId(), avtale.getGjeldendeInnhold().getDeltakerTlf(), SELVBETJENINGSONE_VARSELTEKST);
        assertSmsOpprettetOgSendt(HendelseType.AVTALE_INNGÅTT, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), SELVBETJENINGSONE_VARSELTEKST);
    }

    @Test
    void godkjenningerOpphevet() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(Instant.now(), avtale);
        //Arbeidsgiver opphever deltaker
        arbeidsgiver.opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
        assertSmsOpprettetOgSendt(HendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, avtale.getId(), avtale.getGjeldendeInnhold().getDeltakerTlf(), SELVBETJENINGSONE_VARSELTEKST);

        deltaker.godkjennAvtale(Instant.now(), avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        //Veileder opphever arbeidsgiver og deltaker
        veileder.opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
        assertSmsOpprettetOgSendt(HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, avtale.getId(), avtale.getGjeldendeInnhold().getDeltakerTlf(), SELVBETJENINGSONE_VARSELTEKST);
        assertSmsOpprettetOgSendt(HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), SELVBETJENINGSONE_VARSELTEKST);
    }

    @Test
    void refusjonKlar() {
        Avtale avtale = TestData.enSommerjobbAvtale();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        // I et reelt scenario kan ikke refusjonKlar bli kalt uten at avtalen er godkjent av alle parter+beslutter ++
        avtale.refusjonKlar();
        avtaleRepository.save(avtale);

        String meldingstekst = String.format("Dere kan nå søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Frist for å søke er om to måneder. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_KLAR, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), meldingstekst);
    }
    @Test
    void refusjonKlarRevarsel() {
        Avtale avtale = TestData.enSommerjobbAvtale();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        // I et reelt scenario kan ikke refusjonRevarsel bli kalt uten at avtalen er godkjent av alle parter+beslutter ++
        avtale.refusjonRevarsel();
        avtaleRepository.save(avtale);
        String meldingstekst = String.format("Fristen nærmer seg for å søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_KLAR_REVARSEL, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), meldingstekst);
    }
    @Test
    void refusjonFristForlenget() {
        Avtale avtale = TestData.enSommerjobbAvtale();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        // I et reelt scenario kan ikke refusjonFristForlenget bli kalt uten at avtalen er godkjent av alle parter+beslutter ++
        avtale.refusjonFristForlenget();
        avtaleRepository.save(avtale);
        String meldingstekst = String.format("Fristen for å godkjenne refusjon for avtale med nr: %s har blitt forlenget. Du kan sjekke fristen og søke om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_FRIST_FORLENGET, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), meldingstekst);
    }
    @Test
    void refusjonKorrigert() {
        Avtale avtale = TestData.enSommerjobbAvtale();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        // I et reelt scenario kan ikke refusjonKorrigert bli kalt uten at avtalen er godkjent av alle parter+beslutter ++
        avtale.refusjonKorrigert();
        avtaleRepository.save(avtale);
        String meldingstekst = String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_KORRIGERT, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), meldingstekst);
    }


    @Test
    void refusjonKorrigertKontaktperson__begge_skal_få_sms() {
        Avtale avtale = TestData.enSommerjobbAvtale();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        RefusjonKontaktperson refusjonKontaktperson = new RefusjonKontaktperson("Per", "Persen", "49876543", true);
        avtale.getGjeldendeInnhold().setRefusjonKontaktperson(refusjonKontaktperson);
        // I et reelt scenario kan ikke refusjonKorrigert bli kalt uten at avtalen er godkjent av alle parter+beslutter ++
        avtale.refusjonKorrigert();
        avtaleRepository.save(avtale);
        String meldingstekst = String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_KORRIGERT, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), meldingstekst);
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_KORRIGERT, avtale.getId(), avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(), meldingstekst);
    }

    @Test
    void refusjonKorrigertKontaktperson__bare_kontaktperson_skal_få_sms() {
        Avtale avtale = TestData.enSommerjobbAvtale();
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("41234567");
        RefusjonKontaktperson refusjonKontaktperson = new RefusjonKontaktperson("Per", "Persen", "49876543", false);
        avtale.getGjeldendeInnhold().setRefusjonKontaktperson(refusjonKontaktperson);
        // I et reelt scenario kan ikke refusjonKorrigert bli kalt uten at avtalen er godkjent av alle parter+beslutter ++
        avtale.refusjonKorrigert();
        avtaleRepository.save(avtale);
        String meldingstekst = String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        assertSmsIkkeOpprettetEllerSendt(HendelseType.REFUSJON_KORRIGERT, avtale.getId(), avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), meldingstekst);
        assertSmsOpprettetOgSendt(HendelseType.REFUSJON_KORRIGERT, avtale.getId(), avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(), meldingstekst);
    }

    private void assertSmsOpprettetOgSendt(HendelseType hendelseType, UUID avtaleId, String telefonnummer, String meldingstekst) {
        assertThat(smsRepository.findAll())
                .filteredOn(sms -> sms.getHendelseType() == hendelseType
                        && sms.getAvtaleId().equals(avtaleId)
                        && sms.getTelefonnummer().equals(telefonnummer)
                        && sms.getMeldingstekst().equals(meldingstekst))
                .hasSize(1);
        verify(smsProducer).sendSmsVarselMeldingTilKafka(argThat((Sms sms) ->
                sms.getAvtaleId().equals(avtaleId)
                && sms.getHendelseType().equals(hendelseType)
                && sms.getTelefonnummer().equals(telefonnummer)
                && sms.getMeldingstekst().equals(meldingstekst)));
    }

    private void assertSmsIkkeOpprettetEllerSendt(HendelseType hendelseType, UUID avtaleId, String telefonnummer, String meldingstekst) {
        assertThat(smsRepository.findAll())
                .filteredOn(sms -> sms.getHendelseType() == hendelseType
                        && sms.getAvtaleId().equals(avtaleId)
                        && sms.getTelefonnummer().equals(telefonnummer)
                        && sms.getMeldingstekst().equals(meldingstekst))
                .hasSize(0);
        verify(smsProducer, never()).sendSmsVarselMeldingTilKafka(argThat((Sms sms) ->
                sms.getAvtaleId().equals(avtaleId)
                        && sms.getHendelseType().equals(hendelseType)
                        && sms.getTelefonnummer().equals(telefonnummer)
                        && sms.getMeldingstekst().equals(meldingstekst)));
    }
}