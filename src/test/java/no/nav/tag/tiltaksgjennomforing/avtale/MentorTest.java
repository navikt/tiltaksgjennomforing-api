package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import org.junit.jupiter.api.Test;

public class MentorTest {

    private TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    private AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
    private AxsysService axsysService = mock(AxsysService.class);

    @Test
    public void hentAlleAvtalerMedMuligTilgang__mentor_en_avtale() {

        // GITT
        Avtale avtaleUsignert = TestData.enMentorAvtaleUsignert();
        Avtale avtaleSignert = TestData.enMentorAvtaleSignert();
        avtaleUsignert.setMentorFnr(new Fnr("00000000000"));
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        when(avtaleRepository.findAllByMentorFnr(any())).thenReturn(List.of(avtaleUsignert,avtaleSignert));

        List<Avtale> avtaler = mentor.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

        assertThat(avtaler.size()).isEqualTo(2);
        assertThat(avtaler.get(1)).isEqualTo(avtaleSignert);
        assertThat(avtaler.get(0)).isEqualTo(avtaleUsignert);
        assertThat(avtaler.get(1).getDeltakerFnr()).isNull();
        assertThat(avtaler.get(0).getDeltakerFnr()).isNull();
    }

    @Test
    public void hentAvtalerUtenDeltakerFNR() {

        // GITT
        Avtale avtaleUsignert = TestData.enMentorAvtaleSignert();
        avtaleUsignert.setMentorFnr(new Fnr("00000000000"));
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        // NÅR
        when(avtaleRepository.findById(any())).thenReturn(Optional.of(avtaleUsignert));

        Avtale avtale = mentor.hentAvtale(avtaleRepository, avtaleUsignert.getId());

        assertThat(avtale).isEqualTo(avtaleUsignert);
        assertThat(avtale.getDeltakerFnr()).isNull();
    }

    @Test
    public void harTilgangTilAvtale__mentor_en_avtale_annen_mentor() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        avtale.setMentorFnr(new Fnr("77665521872"));
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        boolean hartilgang = mentor.harTilgangTilAvtale(avtale);
        assertFalse(hartilgang);
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__mentor_en_ikke_signert_avtale_skal_returnere_avtale_med_kun_bedrift_navn() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        avtale.getGjeldendeInnhold().setGodkjentTaushetserklæringAvMentor(null);
        avtale.setMentorFnr(new Fnr("00000000000"));
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        when(avtaleRepository.findAllByMentorFnr(any())).thenReturn(List.of(avtale));

        List<Avtale> avtaler = mentor.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

        assertThat(avtaler).isNotEmpty();
        assertThat(avtaler.get(0).getDeltakerFnr()).isNull();
        assertThat(avtaler.get(0).getVeilederNavIdent()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getDeltakerFornavn()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getDeltakerEtternavn()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getVeilederTlf()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getArbeidsgiverKontonummer()).isNull();
    }

    @Test
    public void endreOmMentor__må_være_en_mentor_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        EndreOmMentor endreOmMentor = new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5, 500);
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_FEIL_TILTAKSTYPE, () -> veileder.endreOmMentor(endreOmMentor, avtale));
    }

    @Test
    public void endreOmMentor__setter_riktige_felter() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennAvtale(Instant.now(), avtale);
        assertThat(avtale.getGjeldendeInnhold().getInnholdType()).isEqualTo(AvtaleInnholdType.INNGÅ);
        EndreOmMentor endreOmMentor = new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5, 500);
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
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_IKKE_INNGAATT_AVTALE, () -> veileder.endreOmMentor(new EndreOmMentor("Per", "Persen", "12345678", "litt mentorering", 5, 500), avtale));
    }

}
