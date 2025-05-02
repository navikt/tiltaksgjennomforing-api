package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;


import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.HentOppfolgingsstatusRequest;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.HentOppfolgingsstatusRespons;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FakeUnleash;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig.NORGNAVN_CACHE;
import static no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig.NORG_GEO_ENHET;
import static no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig.VEILARBOPPFOLGING_CACHE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Slf4j
@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class CachingConfigTest {

    private final CacheManager cacheManager;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final Norg2Client norg2Client;
    private final PersondataService persondataService;
    private final FeatureToggleService featureToggleServiceMock = new FeatureToggleService(new FakeUnleash(), null);

    public CachingConfigTest(
            @Autowired CacheManager cacheManager,
            @Autowired VeilarboppfolgingService veilarboppfolgingService,
            @Autowired Norg2Client norg2Client,
            @Autowired PersondataService persondataService
    ){
        this.cacheManager = cacheManager;
        this.veilarboppfolgingService = veilarboppfolgingService;
        this.norg2Client = norg2Client;
        this.persondataService = persondataService;
    }

    private  <T,K> T getCacheValue(String cacheName, K cacheKey, Class<T> clazz) {
        return (T) Objects.requireNonNull(Objects.requireNonNull(cacheManager.getCache(cacheName)).get(cacheKey)).get();
    }

    @Test
    public void sjekk_at_caching_fanger_opp_data_fra_arena_cache() {
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;
        final String ETT_FNR_NR = "00000000000";
        final String ETT_FNR_NR2 = "11111111111";
        final String ETT_FNR_NR3 = "22127748067";

        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        avtale.setDeltakerFnr(new Fnr(ETT_FNR_NR));
        TestData.setGeoNavEnhet(avtale, oppfolgingNavEnhet);
        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        veilarboppfolgingService.hentOppfolgingsstatus(avtale.getDeltakerFnr().asString());
        veilarboppfolgingService.hentOppfolgingsstatus(ETT_FNR_NR2);
        veilarboppfolgingService.hentOppfolgingsstatus(ETT_FNR_NR2);
        veilarboppfolgingService.hentOppfolgingsstatus(ETT_FNR_NR3);
        veilarboppfolgingService.hentOppfolgingsstatus(ETT_FNR_NR2);

        Assertions.assertEquals(
            "0906",
            getCacheValue(VEILARBOPPFOLGING_CACHE, new HentOppfolgingsstatusRequest(ETT_FNR_NR),
            HentOppfolgingsstatusRespons.class
        ).oppfolgingsenhet().enhetId());
        Assertions.assertEquals(
            "0904",
            getCacheValue(VEILARBOPPFOLGING_CACHE, new HentOppfolgingsstatusRequest(ETT_FNR_NR2),
            HentOppfolgingsstatusRespons.class
        ).oppfolgingsenhet().enhetId());
        Assertions.assertEquals(
            "0906",
            getCacheValue(VEILARBOPPFOLGING_CACHE, new HentOppfolgingsstatusRequest(ETT_FNR_NR3),
            HentOppfolgingsstatusRespons.class
        ).oppfolgingsenhet().enhetId());
    }

    @Test
    public void sjekk_at_caching_fanger_opp_data_fra_norgnavn_cache() {
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();

        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        Norg2OppfølgingResponse norg2OppfølgingResponse = norg2Client.hentOppfølgingsEnhetFraCacheNorg2(
                avtale.getEnhetOppfolging()
        );
        Norg2OppfølgingResponse norgnavnCacheForEnhet = getCacheValue(
                NORGNAVN_CACHE,
                avtale.getEnhetOppfolging(),
                Norg2OppfølgingResponse.class
        );

        Assertions.assertEquals("NAV Agder", norgnavnCacheForEnhet.getNavn());
        Assertions.assertEquals("1000", norgnavnCacheForEnhet.getEnhetNr());
        Assertions.assertEquals(norg2OppfølgingResponse.getNavn(), norgnavnCacheForEnhet.getNavn());
        Assertions.assertEquals(norg2OppfølgingResponse.getEnhetNr(), norgnavnCacheForEnhet.getEnhetNr());
    }

    @Test
    public void sjekk_at_caching_fanger_opp_data_fra_norggeoenhet_cache() {
        String geoEnhet = "030104";

        Norg2GeoResponse norg2GeoResponse = norg2Client.hentGeoEnhetFraCacheEllerNorg2(geoEnhet);
        Norg2GeoResponse norggeoenhetCacheForGeoEnhet = getCacheValue(NORG_GEO_ENHET, geoEnhet, Norg2GeoResponse.class);

        Assertions.assertEquals("NAV St. Hanshaugen", norggeoenhetCacheForGeoEnhet.getNavn());
        Assertions.assertEquals("0313", norggeoenhetCacheForGeoEnhet.getEnhetNr());
        Assertions.assertEquals(norg2GeoResponse.getNavn(), norggeoenhetCacheForGeoEnhet.getNavn());
        Assertions.assertEquals(norg2GeoResponse.getEnhetNr(), norggeoenhetCacheForGeoEnhet.getEnhetNr());
    }

    @Test
    public void vertifisere_at_caching_fungerer_for_endreAvtale_av_veileder() {
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;
        final String geolokasjon = "030104";
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        TestData.setGeoNavEnhet(avtale, oppfolgingNavEnhet);
        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        final TilgangskontrollService mockTilgangskontrollService = mock(TilgangskontrollService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                mockTilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(new NavEnhet(avtale.getEnhetOppfolging(), avtale.getEnhetsnavnOppfolging())),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class)
        );
        when(mockTilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        lenient().when(mockTilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(veileder),
                eq(avtale.getDeltakerFnr())
        )).thenReturn(true, true, true);

        veileder.endreAvtale(
                Now.instant(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale
        );

        veileder.endreAvtale(
                Now.instant(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale
        );

        Norg2OppfølgingResponse norgnavnCacheForEnhet = getCacheValue(
                NORGNAVN_CACHE,
                avtale.getEnhetOppfolging(),
                Norg2OppfølgingResponse.class
        );
        Norg2GeoResponse norggeoenhetCacheForGeoEnhet = getCacheValue(
                NORG_GEO_ENHET,
                geolokasjon,
                Norg2GeoResponse.class
        );
        HentOppfolgingsstatusRespons arenaCache = getCacheValue(
            VEILARBOPPFOLGING_CACHE,
            new HentOppfolgingsstatusRequest(avtale.getDeltakerFnr().asString()),
            HentOppfolgingsstatusRespons.class
        );

        Assertions.assertEquals("NAV St. Hanshaugen", norggeoenhetCacheForGeoEnhet.getNavn());
        Assertions.assertEquals("0313", norggeoenhetCacheForGeoEnhet.getEnhetNr());

        Assertions.assertEquals("NAV Agder", norgnavnCacheForEnhet.getNavn());
        Assertions.assertEquals("1000", norgnavnCacheForEnhet.getEnhetNr());

        Assertions.assertEquals("0906", arenaCache.oppfolgingsenhet().enhetId());
    }
}
