package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.getForPart;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.jsonHarVerdi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles({ Miljø.LOCAL, Miljø.TEST })
@SpringBootTest
@AutoConfigureMockMvc
public class MentorTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    private AvtaleRepository avtaleRepository;
    @Autowired
    ObjectMapper mapper;

    private final Pageable pageable = PageRequest.of(0, 100);

    @Test
    public void hentAlleAvtalerMedMuligTilgang__mentor_en_avtale() throws Exception {

        // GITT
        Avtale avtaleUsignert = TestData.enMentorAvtaleUsignert();
        Avtale avtaleSignert = TestData.enMentorAvtaleSignert();
        Mentor mentor = TestData.enMentor(avtaleSignert);

        // NÅR
        when(avtaleRepository.findAllByMentorFnrAndFeilregistrertIsFalse(any(), any())).thenReturn(new PageImpl<>(List.of(avtaleUsignert, avtaleSignert)));
        var avtalerRespons = hentAvtalerForMentor(mentor);
        var avtalerMinimal = mapper.readValue(avtalerRespons.getContentAsString(), HashMap.class);

        assertThat(((List<?>) avtalerMinimal.get("avtaler")).size()).isEqualTo(2);
        assertFalse(jsonHarVerdi(avtalerRespons.getContentAsString(), avtaleSignert.getDeltakerFnr().asString()));
        assertFalse(jsonHarVerdi(avtalerRespons.getContentAsString(), avtaleUsignert.getDeltakerFnr().asString()));
    }

    @Test
    public void deltakerFNR_skal_være_null_selv_om_mentor_har_signert() throws Exception {
        // GITT
        Avtale avtaleSignert = TestData.enMentorAvtaleSignert();
        var originalJson = mapper.valueToTree(avtaleSignert);
        var deltakerFnr = avtaleSignert.getDeltakerFnr().asString();
        Mentor mentor = TestData.enMentor(avtaleSignert);
        // NÅR
        when(avtaleRepository.findById(any())).thenReturn(Optional.of(avtaleSignert));
        var respons = hentAvtaleForMentor(mentor, avtaleSignert.getId());
        var avtaleJson = mapper.readTree(respons.getContentAsString(StandardCharsets.UTF_8));

        assertEquals(avtaleJson, ((ObjectNode) originalJson.deepCopy()).putNull("deltakerFnr"),
                "Når vi fjerner fnr fra det opprinnelige testobjektet skal det matche responsen");
        assertFalse(jsonHarVerdi(respons.getContentAsString(), deltakerFnr));
    }

    @Test
    public void om_mentor_har_tilgang_til_en_annen_mentors_avtale() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = new Mentor(new Fnr("77665521872"));
        // NÅR
        boolean hartilgang = mentor.harTilgangTilAvtale(avtale);
        assertFalse(hartilgang);
    }

    @Test
    public void om_mentor_har_tilgang_til_en_annen_mentors_avtale_TestDataTest() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = new Mentor(new Fnr("77665521872"));
        // NÅR
        boolean hartilgang = mentor.harTilgangTilAvtale(avtale);
        assertFalse(hartilgang);
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__mentor_en_ikke_signert_avtale_skal_returnere_avtale_med_kun_bedrift_navn() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = TestData.enMentor(avtale);
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        when(avtaleRepository.findAllByMentorFnrAndFeilregistrertIsFalse(any(), eq(pageable))).thenReturn(new PageImpl<>(List.of(avtale)));
        Map<String, Object> avtalerMinimalPage = mentor.hentAlleAvtalerMedLesetilgang(avtaleRepository, avtalePredicate, null, pageable);
        List<AvtaleMinimalListevisning> avtalerMinimal = (List<AvtaleMinimalListevisning>) avtalerMinimalPage.get("avtaler");

        assertThat(avtalerMinimal).isNotEmpty();
        assertThat(avtalerMinimal.get(0).getBedriftNavn()).isNotNull();
    }

    @Test
    public void endreOmMentor__må_være_en_mentor_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        EndreOmMentor endreOmMentor = new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5.0, 500);
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_FEIL_TILTAKSTYPE, () -> veileder.endreOmMentor(endreOmMentor, avtale));
    }

    @Test
    public void endreOmMentor__setter_riktige_felter() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        assertThat(avtale.getGjeldendeInnhold().getInnholdType()).isEqualTo(AvtaleInnholdType.INNGÅ);
        EndreOmMentor endreOmMentor = new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5.0, 500);
        veileder.endreOmMentor(endreOmMentor, avtale);
        assertThat(avtale.getGjeldendeInnhold().getMentorFornavn()).isEqualTo("Per");
        assertThat(avtale.getGjeldendeInnhold().getMentorEtternavn()).isEqualTo("Persen");
        assertThat(avtale.getGjeldendeInnhold().getMentorTlf()).isEqualTo("12345678");
        assertThat(avtale.getGjeldendeInnhold().getMentorOppgaver()).isEqualTo("litt mentorering");
        assertThat(avtale.getGjeldendeInnhold().getMentorAntallTimer()).isEqualTo(5);
        assertThat(avtale.getGjeldendeInnhold().getMentorTimelonn()).isEqualTo(500);
        assertThat(avtale.getGjeldendeInnhold().getInnholdType()).isEqualTo(AvtaleInnholdType.ENDRE_OM_MENTOR);
    }

    @Test
    public void endreOmMentor__avtale_må_være_inngått() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        assertThat(avtale.erAvtaleInngått()).isFalse();
        assertFeilkode(
                Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_IKKE_INNGAATT_AVTALE,
                () -> veileder.endreOmMentor(new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5.0, 500), avtale)
        );
    }

    private MockHttpServletResponse hentAvtalerForMentor(Mentor mentor) throws Exception {
        return getForPart(mockMvc, mentor, "/avtaler");
    }

    private MockHttpServletResponse hentAvtaleForMentor(Mentor mentor, UUID avtaleId) throws Exception {
        return getForPart(mockMvc, mentor, "/avtaler/" + avtaleId);
    }

}
