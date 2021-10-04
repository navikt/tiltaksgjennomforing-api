package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed.NyBeskjedResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyOppgave.NyOppgaveResponse;
import org.junit.Before;
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
public class NotifikasjonServiceTest {

    @Autowired
    NotifikasjonService notifikasjonService;

    @Autowired
    NotifikasjonParser parser;

    Avtale avtale;
    ArbeidsgiverNotifikasjon notifikasjon;

    // TODO: Fjerne response-type fra opprettNyOppgave og opprettNyBeskjed. bruk heller sporring mot tabell at notifikasjon har blitt gjennomfort vellykket.

    // TODO: skriv tester to oppgaveUtfoert
    // TODO: skriv metode og tester for softDeleteOppgave

    @Before
    public void init() {
        avtale = TestData.enArbeidstreningAvtale();
        notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(
                        avtale,
                        VarslbarHendelseType.OPPRETTET,
                        notifikasjonService,
                        parser);
    }

    @Test
    public void opprettNyBeskjedTest() {
        final NyBeskjedResponse response =
                notifikasjonService.opprettNyBeskjed(
                        notifikasjon,
                        NotifikasjonMerkelapp.TILTAK,
                        NotifikasjonTekst.AVTALE_OPPRETTET);
        assertThat(response.getData().getNyBeskjed().get__typename()).isNotEmpty();
    }

    @Test
    public void opprettNyOppgaveTest() {
        final NyOppgaveResponse response =
                notifikasjonService.opprettOppgave(
                        notifikasjon,
                        NotifikasjonMerkelapp.TILTAK,
                        NotifikasjonTekst.AVTALE_OPPRETTET);
        assertThat(response.getData().getNyOppgave().get__typename()).isNotEmpty();
    }
}
