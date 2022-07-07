package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
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
        avtaleUsignert.setMentorFnr("00000000000");
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        when(avtaleRepository.findAllByMentorFnr(anyString())).thenReturn(List.of(avtaleUsignert,avtaleSignert));

        List<Avtale> avtaler = mentor.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

        assertThat(avtaler.size()).isEqualTo(2);
        assertThat(avtaler.get(1)).isEqualTo(avtaleSignert);
        assertThat(avtaler.get(0)).isEqualTo(avtaleUsignert);
    }

    @Test
    public void harTilgangTilAvtale__mentor_en_avtale_annen_mentor() {

        // GITT
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        avtale.setMentorFnr("77665521872");
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
        avtale.setMentorFnr("00000000000");
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        when(avtaleRepository.findAllByMentorFnr(anyString())).thenReturn(List.of(avtale));

        List<Avtale> avtaler = mentor.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

        assertThat(avtaler).isNotEmpty();
        assertThat(avtaler.get(0).getDeltakerFnr()).isNull();
        assertThat(avtaler.get(0).getVeilederNavIdent()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getDeltakerFornavn()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getDeltakerEtternavn()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getVeilederTlf()).isNull();
        assertThat(avtaler.get(0).getGjeldendeInnhold().getArbeidsgiverKontonummer()).isNull();
    }


}
