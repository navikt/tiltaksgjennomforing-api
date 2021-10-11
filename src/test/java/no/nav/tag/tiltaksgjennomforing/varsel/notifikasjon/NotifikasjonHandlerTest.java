package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.exceptions.KallTiArbeidsgiverNotifikasjonFeiletException;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.CommonResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL })
@DirtiesContext
public class NotifikasjonHandlerTest {


    @Autowired
    private NotifikasjonHandler notifikasjonHandler;

    @MockBean
    private ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    @Test
    public void sjekkOgSettStatusResponseTest(){

        ArbeidsgiverNotifikasjon arbeidsgiverNotifikasjon = new ArbeidsgiverNotifikasjon();
        CommonResponse commenResponse = new CommonResponse("ertert","345345", "dgdgdfgsd");
        MutationStatus mutationStatus = MutationStatus.NY_OPPGAVE_VELLYKKET;

        notifikasjonHandler.sjekkOgSettStatusResponse(arbeidsgiverNotifikasjon,commenResponse, mutationStatus);

        Mockito.verify(arbeidsgiverNotifikasjonRepository).save(any());
    }

    @Test
    public void sjekkOgSettStatusResponseErNullTest(){

        ArbeidsgiverNotifikasjon arbeidsgiverNotifikasjon = new ArbeidsgiverNotifikasjon();
        CommonResponse commenResponse = null;
        MutationStatus mutationStatus = MutationStatus.NY_OPPGAVE_VELLYKKET;


        assertThrows(KallTiArbeidsgiverNotifikasjonFeiletException.class, () -> {
            notifikasjonHandler.sjekkOgSettStatusResponse(arbeidsgiverNotifikasjon,commenResponse, mutationStatus);
        });
    }
}
