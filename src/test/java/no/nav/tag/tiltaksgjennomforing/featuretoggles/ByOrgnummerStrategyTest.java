package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.UnleashContext;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangerDto;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ByOrgnummerStrategyTest {

    private UnleashContext unleashContext;

    @Mock
    AltinnTilgangsstyringService altinnTilgangsstyringService;

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;

        Fnr fnr = Fnr.generer(2001, 11, 12);
        unleashContext = UnleashContext.builder().userId(fnr.asString()).build();
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void skal_være_enablet_hvis_bruker_tilhører_organisasjon() {
        when(altinnTilgangsstyringService.hentAltinnTilganger()).thenReturn(altinnTilganger("999999999"));
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isTrue();
    }

    @Test
    public void skal_være_disablet_hvis_bruker_ikke_tilhører_organisasjon() {
        when(altinnTilgangsstyringService.hentAltinnTilganger()).thenReturn(altinnTilganger("999999998"));
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isFalse();
    }

    @Test
    public void navIdent_skal_returnere_false() {
        UnleashContext unleashContext = UnleashContext.builder().userId("J154200").build();
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isFalse();
        verify(altinnTilgangsstyringService, never()).hentAltinnTilganger();
    }

    @Test
    public void byOrgnummmer_strategy_håndterer_flere_orgnummer() {
        when(altinnTilgangsstyringService.hentAltinnTilganger()).thenReturn(altinnTilganger("999999999"));
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "910825526,999999999"), unleashContext)).isTrue();
    }

    @Test
    public void skal_være_disablet_hvis_feil_ved_oppslag_i_altinn() {
        when(altinnTilgangsstyringService.hentAltinnTilganger()).thenThrow(RuntimeException.class);
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isFalse();
    }

    private AltinnTilgangerDto altinnTilganger(String orgnummer) {
        return new AltinnTilgangerDto(
                List.of(),
                Map.of(new BedriftNr(orgnummer), Set.of(Tiltakstype.ARBEIDSTRENING)),
                List.of()
        );
    }
}
