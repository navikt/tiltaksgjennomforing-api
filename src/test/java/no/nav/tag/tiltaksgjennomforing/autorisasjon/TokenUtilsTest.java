package no.nav.tag.tiltaksgjennomforing.autorisasjon;


import static org.junit.jupiter.api.Assertions.assertTrue;



import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Disabled
@RunWith(MockitoJUnitRunner.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    @Mock
    private TokenValidationContextHolder contextHolder;


    @Test
    public void hentInnloggetBruker__er_selvbetjeningbruker() {
        InnloggetArbeidsgiver selvbetjeningBruker = TestData.enInnloggetArbeidsgiver();
        assertTrue(true);
    }

}