package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed.NyBeskjedResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyOppgave.NyOppgaveResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({Miljø.LOCAL})
@DirtiesContext
public class NotifikasjonMSAServiceTest {

    @Autowired
    NotifikasjonMSAService notifikasjonMSAService;

    Avtale avtale = TestData.enArbeidstreningAvtale();

    @Test
    public void opprettNyBeskjedTest() throws JsonProcessingException {
        final NyBeskjedResponse response =
                notifikasjonMSAService.opprettNyBeskjed(
                        avtale,
                        NotifikasjonMerkelapp.TILTAK_AVTALE_OPPRETTET,
                        NotifikasjonTekst.AVTALE_OPPRETTET);
        assertThat(response.getData().getNyBeskjed().get__typename()).isNotEmpty();
    }

    @Test
    public void opprettNyOppgaveTest() throws JsonProcessingException {
        final NyOppgaveResponse response =
                notifikasjonMSAService.opprettOppgave(
                        avtale,
                        NotifikasjonMerkelapp.TILTAK_AVTALE_OPPRETTET,
                        NotifikasjonTekst.AVTALE_OPPRETTET);
        assertThat(response.getData().getNyOppgave().get__typename()).isNotEmpty();
    }
}
