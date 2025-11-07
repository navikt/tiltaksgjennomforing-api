package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.getForPart;
import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleApiTestUtil.jsonHarVerdi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@AutoConfigureMockMvc
public class MentorTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    private AvtaleRepository avtaleRepository;
    @Autowired
    ObjectMapper mapper;

    private final Pageable pageable = PageRequest.of(0, 100);

    @Test
    public void endreAvtale_av_veileder_fra_opphav_arena_og_start_dato_er_allerede_satt_skal_ikke_kunne_endres(){
        // GITT
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MENTOR), Avtaleopphav.ARENA);
        Avtale arenaAvtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MENTOR), Avtaleopphav.ARENA);
        arenaAvtale.getGjeldendeInnhold().setStartDato(LocalDate.now().minusYears(1));

        ArenaRyddeAvtale arenaRyddeAvtale = new ArenaRyddeAvtale();
        arenaRyddeAvtale.setAvtale(arenaAvtale);
        arenaRyddeAvtale.setMigreringsdato(LocalDate.now());

        avtale.setArenaRyddeAvtale(arenaRyddeAvtale);
        avtale.getGjeldendeInnhold().setAvtale(avtale);

        avtale.setGodkjentForEtterregistrering(true); // DENNE SETTES TIL TRUE UNDER ARENA MIGRERING
        avtale.getGjeldendeInnhold().setStartDato(LocalDate.now().minusYears(2));
        avtale.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusYears(1));

        // NÅR
        val ønsketStartDato_2_maaneder_tilbake = LocalDate.now().minusMonths(2);
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setStartDato(ønsketStartDato_2_maaneder_tilbake);
        // SÅ
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_AVTALE_MED_OPPHAV_ARENA_OG_STARTDATO_ALLEREDE_SATT,() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER));
    }

    @Test
    public void endreAvtale_av_veileder_fra_opphav_arena_og_start_dato_er_IKKE_satt_skal_avtalen_dato_kunne_endres(){
        // GITT
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MENTOR), Avtaleopphav.ARENA);
        Avtale arenaAvtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MENTOR), Avtaleopphav.ARENA);

        ArenaRyddeAvtale arenaRyddeAvtale = new ArenaRyddeAvtale();
        arenaRyddeAvtale.setAvtale(arenaAvtale);
        arenaRyddeAvtale.setMigreringsdato(LocalDate.now());

        avtale.setArenaRyddeAvtale(arenaRyddeAvtale);
        avtale.getGjeldendeInnhold().setAvtale(avtale);

        avtale.setGodkjentForEtterregistrering(true); // DENNE SETTES TIL TRUE UNDER ARENA MIGRERING
        avtale.getGjeldendeInnhold().setStartDato(null);
        avtale.getGjeldendeInnhold().setSluttDato(null);

        //NÅR
        val ønsketStartDato_2_maaneder_tilbake = LocalDate.now().minusMonths(2);
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setStartDato(ønsketStartDato_2_maaneder_tilbake);
        endreAvtale.setSluttDato(ønsketStartDato_2_maaneder_tilbake.plusMonths(1));

        // SÅ
        assertDoesNotThrow(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER));
        assertThat(avtale.getGjeldendeInnhold().getStartDato()).isEqualTo(ønsketStartDato_2_maaneder_tilbake);
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(ønsketStartDato_2_maaneder_tilbake.plusMonths(1));
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__mentor_en_avtale() throws Exception {

        // GITT
        Avtale avtaleUsignert = TestData.enMentorAvtaleUsignert();
        Avtale avtaleSignert = TestData.enMentorAvtaleSignert();
        Mentor mentor = TestData.enMentor(avtaleSignert);

        // NÅR
        when(avtaleRepository.findAllByMentorFnr(any(), any())).thenReturn(new PageImpl<>(List.of(
            avtaleUsignert,
            avtaleSignert
        )));
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

        assertEquals(avtaleJson.get("deltakerFnr"), ((ObjectNode) originalJson.deepCopy()).putNull("deltakerFnr").get("deltakerFnr"),
                "Når vi fjerner fnr fra det opprinnelige testobjektet skal det matche responsen");
        assertFalse(jsonHarVerdi(respons.getContentAsString(), deltakerFnr));
    }

    @Test
    public void om_mentor_har_tilgang_til_en_annen_mentors_avtale() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = new Mentor(Fnr.generer(1956, 7, 8));
        // NÅR
        boolean hartilgang = mentor.harTilgangTilAvtale(avtale).erTillat();
        assertFalse(hartilgang);
    }

    @Test
    public void om_mentor_har_tilgang_til_en_annen_mentors_avtale_TestDataTest() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = new Mentor(Fnr.generer(1956, 7, 8));
        // NÅR
        boolean hartilgang = mentor.harTilgangTilAvtale(avtale).erTillat();
        assertFalse(hartilgang);
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__mentor_en_ikke_signert_avtale_skal_returnere_avtale_med_kun_bedrift_navn() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = TestData.enMentor(avtale);
        AvtaleQueryParameter avtalePredicate = new AvtaleQueryParameter();
        // NÅR
        when(avtaleRepository.findAllByMentorFnr(any(), eq(pageable))).thenReturn(new PageImpl<>(List.of(avtale)));
        List<BegrensetAvtale> avtalerMinimal  = mentor
            .hentBegrensedeAvtalerMedLesetilgang(avtaleRepository, avtalePredicate, pageable)
            .getContent();

        assertThat(avtalerMinimal).isNotEmpty();
        assertThat(avtalerMinimal.get(0).bedriftNavn()).isNotNull();
    }

    @Test
    public void endreOmMentor__må_være_en_mentor_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        EndreOmMentor endreOmMentor = new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5.0, 500);
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_FEIL_TILTAKSTYPE, () -> veileder.endreOmMentor(endreOmMentor, avtale));
    }

    @Test
    public void endreOmMentor__setter_riktige_felter() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        arbeidsgiver.godkjennAvtale(avtale);
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
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        arbeidsgiver.godkjennAvtale(avtale);
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
