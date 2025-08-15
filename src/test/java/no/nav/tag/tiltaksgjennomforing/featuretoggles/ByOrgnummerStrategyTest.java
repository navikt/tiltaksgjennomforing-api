package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.UnleashContext;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ByOrgnummerStrategyTest {

    private Fnr fnr;
    private UnleashContext unleashContext;

    @Mock
    AltinnTilgangsstyringService altinnTilgangsstyringService;

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;

        fnr = Fnr.generer(2001, 11, 12);
        unleashContext = UnleashContext.builder().userId(fnr.asString()).build();
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void skal_være_enablet_hvis_bruker_tilhører_organisasjon() {
        Set<AltinnReportee> orgSet = new HashSet<>();
        orgSet.add(new AltinnReportee("", "AS", null, "999999999", "", "", null));

        when(altinnTilgangsstyringService.hentAltinnOrganisasjoner(eq(fnr), any())).thenReturn(orgSet);
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isTrue();
    }

    @Test
    public void skal_være_disablet_hvis_bruker_ikke_tilhører_organisasjon() {
        Set<AltinnReportee> orgSet = new HashSet<>();
        orgSet.add(new AltinnReportee("", "AS", null, "999999998", "", "", null));

        when(altinnTilgangsstyringService.hentAltinnOrganisasjoner(fnr, () -> "")).thenReturn(orgSet);
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isFalse();
    }

    @Test
    public void navIdent_skal_returnere_false() {
        UnleashContext unleashContext = UnleashContext.builder().userId("J154200").build();
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isFalse();
        verify(altinnTilgangsstyringService, never()).hentAltinnOrganisasjoner(any(), any());
    }

    @Test
    public void byOrgnummmer_strategy_håndterer_flere_orgnummer() {
        Set<AltinnReportee> orgSet = new HashSet<>();
        orgSet.add(new AltinnReportee("", "AS", null, "999999999", "", "", null));

        when(altinnTilgangsstyringService.hentAltinnOrganisasjoner(eq(fnr), any())).thenReturn(orgSet);
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "910825526,999999999"), unleashContext)).isTrue();
    }

    @Test
    public void skal_være_disablet_hvis_feil_ved_oppslag_i_altinn() {
        when(altinnTilgangsstyringService.hentAltinnOrganisasjoner(fnr, () -> "")).thenThrow(RuntimeException.class);
        assertThat(new ByOrgnummerStrategy(altinnTilgangsstyringService).isEnabled(Map.of(ByOrgnummerStrategy.UNLEASH_PARAMETER_ORGNUMRE, "999999999"), unleashContext)).isFalse();
    }

}
