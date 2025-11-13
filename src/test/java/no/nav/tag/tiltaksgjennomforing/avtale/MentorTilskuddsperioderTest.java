package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.TilskuddsperiodeKafkaProducer;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
    "tiltaksgjennomforing.kafka.enabled=true",
    "tiltaksgjennomforing.mentor-tilskuddsperioder.enabled=true"
})
@DirtiesContext
@ActiveProfiles(Miljø.TEST)
@EmbeddedKafka(partitions = 1, topics = {Topics.AVTALE_HENDELSE_COMPACT})
class MentorTilskuddsperioderTest {

    @Autowired
    SmsRepository smsRepository;
    @Autowired
    AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;
    @Autowired
    AvtaleRepository avtaleRepository;
    @MockitoSpyBean
    TilskuddsperiodeKafkaProducer TilskuddsperiodeKafkaProducer;

    @Test
    void enabledMentorTilskuddsperiodeFeatureToggle() {
        MentorTilskuddsperioderToggle.setValue(true);
        assertThat(MentorTilskuddsperioderToggle.isEnabled()).isTrue();
    }
    @Test
    void disabledMentorTilskuddsperiodeFeatureToggle() {
        MentorTilskuddsperioderToggle.setValue(false);
        assertThat(MentorTilskuddsperioderToggle.isEnabled()).isFalse();
    }

    @Test
    void avtaleInngåttMentorTilskuddsperioder() throws JsonProcessingException {
        MentorTilskuddsperioderToggle.setValue(true);

        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Mentor mentor = TestData.enMentor(avtale);
        Veileder veileder = TestData.enVeileder(avtale);

        mentor.godkjennAvtale(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        GodkjentPaVegneGrunn grunn = new GodkjentPaVegneGrunn();
        grunn.setIkkeBankId(true);
        veileder.godkjennForVeilederOgDeltaker(grunn, avtale);

        assertIngenIngåttMelding(avtale.getId());

        Beslutter beslutter = TestData.enBeslutter(avtale);
        beslutter.godkjennTilskuddsperiode(avtale, TestData.ENHET_OPPFØLGING.getVerdi());
        avtaleRepository.saveAndFlush(avtale);

        assertIngåttMeldingOpprettetOgIngått(avtale.getId());

        verify(
            TilskuddsperiodeKafkaProducer,
            times(1)
        ).publiserTilskuddsperiodeGodkjentMelding(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void avtaleMentorUtenTilskuddsperioderMedToggleAv() throws JsonProcessingException {
        MentorTilskuddsperioderToggle.setValue(false);

        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Mentor mentor = TestData.enMentor(avtale);
        Veileder veileder = TestData.enVeileder(avtale);

        mentor.godkjennAvtale(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        GodkjentPaVegneGrunn grunn = new GodkjentPaVegneGrunn();
        grunn.setIkkeBankId(true);
        veileder.godkjennForVeilederOgDeltaker(grunn, avtale);

        assertIngenIngåttMelding(avtale.getId());
        Beslutter beslutter = TestData.enBeslutter(avtale);

        assertThatThrownBy(() ->
            beslutter.godkjennTilskuddsperiode(avtale, TestData.ENHET_OPPFØLGING.getVerdi())
        ).isInstanceOf(NullPointerException.class).hasMessageContaining("gjeldendePeriode");

        verify(
            TilskuddsperiodeKafkaProducer,
            times(0)
        ).publiserTilskuddsperiodeGodkjentMelding(org.mockito.ArgumentMatchers.any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @Disabled("mentorTimelonn vil bli satt dersom man fyller inn valgtLonnstype+valgtLonnstypeBelop, så testriggen her klarer ikke sjekke at 'alt er fylt ut utenom timelonn'")
    public void kanIkkeGodkjennesNårNoeMangler(boolean mentorTilskuddsperioderEnabled) {
        MentorTilskuddsperioderToggle.setValue(mentorTilskuddsperioderEnabled);

        Set<String> mentorTilskuddsperioderFelter = Set.of(
            AvtaleInnhold.Fields.arbeidsgiveravgift,
            AvtaleInnhold.Fields.otpSats,
            AvtaleInnhold.Fields.feriepengesats,
            AvtaleInnhold.Fields.arbeidsgiverKontonummer,
            AvtaleInnhold.Fields.mentorValgtLonnstype,
            AvtaleInnhold.Fields.mentorValgtLonnstypeBelop
        );

        Set<String> mentorfelter = new HashSet<>(Set.of(
            AvtaleInnhold.Fields.deltakerFornavn,
            AvtaleInnhold.Fields.deltakerEtternavn,
            AvtaleInnhold.Fields.deltakerTlf,
            AvtaleInnhold.Fields.bedriftNavn,
            AvtaleInnhold.Fields.arbeidsgiverFornavn,
            AvtaleInnhold.Fields.arbeidsgiverEtternavn,
            AvtaleInnhold.Fields.arbeidsgiverTlf,
            AvtaleInnhold.Fields.veilederFornavn,
            AvtaleInnhold.Fields.veilederEtternavn,
            AvtaleInnhold.Fields.veilederTlf,
            AvtaleInnhold.Fields.startDato,
            AvtaleInnhold.Fields.sluttDato,
            AvtaleInnhold.Fields.mentorFornavn,
            AvtaleInnhold.Fields.mentorEtternavn,
            AvtaleInnhold.Fields.mentorOppgaver,
            AvtaleInnhold.Fields.mentorAntallTimer,
            AvtaleInnhold.Fields.mentorTimelonn,
            AvtaleInnhold.Fields.tilrettelegging,
            AvtaleInnhold.Fields.oppfolging,
            AvtaleInnhold.Fields.mentorTlf,
            AvtaleInnhold.Fields.harFamilietilknytning
        ));

        Avtale avtale = Avtale.opprett(
            new OpprettMentorAvtale(
                TestData.etFodselsnummer(),
                TestData.etFodselsnummer(),
                TestData.etBedriftNr(),
                Tiltakstype.MENTOR,
                Avtalerolle.VEILEDER
            ), Avtaleopphav.VEILEDER, TestData.enNavIdent()
        );


        if (mentorTilskuddsperioderEnabled) {
            mentorfelter.addAll(mentorTilskuddsperioderFelter);
        }

        AvtaleTest.testAtAlleFelterMangler(avtale, mentorfelter);
        AvtaleTest.testAtHvertEnkeltFeltMangler(avtale, mentorfelter, avtale.getTiltakstype());
    }

    private void assertIngenIngåttMelding(UUID avtaleId) throws JsonProcessingException {
        assertThat(avtaleMeldingEntitetRepository.findAllByAvtaleId(avtaleId))
            .filteredOn(avtaleMelding -> avtaleMelding.getHendelseType() == HendelseType.AVTALE_INNGÅTT)
            .isEmpty();
    }

    private void assertIngåttMeldingOpprettetOgIngått(UUID avtaleId) throws JsonProcessingException {
        assertThat(avtaleMeldingEntitetRepository.findAllByAvtaleId(avtaleId))
            .filteredOn(avtaleMelding ->
                avtaleMelding.getHendelseType() == HendelseType.AVTALE_INNGÅTT &&
                    avtaleMelding.getAvtaleStatus().equals(Status.GJENNOMFØRES)
            )
            .hasSize(1);
    }
}
