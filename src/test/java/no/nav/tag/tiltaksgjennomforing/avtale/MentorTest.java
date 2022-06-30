package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MentorTest {

    private TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    private AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
    private AxsysService axsysService = mock(AxsysService.class);

    @Test
    public void hentAlleAvtalerMedMuligTilgang__hent_ingen_GODKJENTE_når_avtaler_har_gjeldende_tilskuddsperiodestatus_ubehandlet() {

        // GITT
        Avtale avtale = TestData.enMentorAvtale();
        avtale.setMentorFnr("00000000000");
        Mentor mentor = new Mentor(new Fnr("00000000000"));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        // NÅR
        when(avtaleRepository.findAllByMentorFnr(anyString())).thenReturn(List.of(avtale));

        List<Avtale> avtaler = mentor.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

        assertThat(avtaler).isNotEmpty();
    }


}
