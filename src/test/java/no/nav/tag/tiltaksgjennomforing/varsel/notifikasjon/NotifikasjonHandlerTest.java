package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.FellesResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed.NyBeskjedResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyOppgave.NyOppgaveResponse;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({Miljø.LOCAL})
@DirtiesContext
public class NotifikasjonHandlerTest {

    @Autowired
    private NotifikasjonHandler notifikasjonHandler;

    @MockBean
    private ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    String response;

    @BeforeEach
    public void init() {
        response = "{\n" +
                "  \"data\": {\n" +
                "    \"nyBeskjed\": {\n" +
                "      \"__typename\": \"NyBeskjedVellykket\",\n" +
                "      \"id\": \"d69f8c4f-8d34-47b0-9539-d3c2e54115da\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    @Test
    public void sjekkOgSettStatusResponseTest() {
        ArbeidsgiverNotifikasjon arbeidsgiverNotifikasjon = new ArbeidsgiverNotifikasjon();
        FellesResponse response = new FellesResponse("" +
                MutationStatus.NY_OPPGAVE_VELLYKKET.getStatus(),
                "231a0f8c-237c-4357-8101-6a356a9ace86",
                "nihil ut eum alias saepe nesciunt minima");
        MutationStatus mutationStatus = MutationStatus.NY_OPPGAVE_VELLYKKET;

        notifikasjonHandler.sjekkOgSettStatusResponse(arbeidsgiverNotifikasjon, response, mutationStatus);

        Mockito.verify(arbeidsgiverNotifikasjonRepository).save(any());
    }

    @Test
    public void readResponseTest() {
        final NyBeskjedResponse nyBeskjedResponse = notifikasjonHandler.readResponse(response, NyBeskjedResponse.class);
        final NyOppgaveResponse feiletObjectMapping =
                notifikasjonHandler.readResponse(response, NyOppgaveResponse.class);

        assertThat(nyBeskjedResponse.getData().getNyBeskjed()).isNotNull();
        assertThat(feiletObjectMapping.getData().getNyOppgave()).isNull();
    }

    @Test
    @Ignore
    public void readResponseNarAPIsenderError(){
        final String error ="{ \"errors\" :[ { \"message\": \"Field 'eksternId' of variable 'eksternId' has coerced Null value for NonNull type 'ID!'\"," +
                                            " \"locations\":[ { \"line\":1, \"column\":36 } ], \"extensions\":{ \"classification\": \"ValidationError\" } } ] }";
    }

    @Test
    public void convertResponseTest() {
        final NyBeskjedResponse nyBeskjedResponse = notifikasjonHandler.readResponse(response, NyBeskjedResponse.class);
        FellesResponse fellesResponse =
                notifikasjonHandler.konverterResponse(nyBeskjedResponse.getData().getNyBeskjed());
        FellesResponse fellesResponseFeilet =
                notifikasjonHandler.konverterResponse(nyBeskjedResponse.getData());

        assertThat(fellesResponse.getId()).isNotNull();
        assertThat(fellesResponseFeilet.getId()).isNull();

    }
}
