package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;


import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddsperiodeConfig;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.EhCacheConfig;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CachingConfigMockTest {

    @Mock
    private CacheManager cacheManager;

    @BeforeAll
    public void setup() {
        Cache arenaCache = new ConcurrentMapCache(EhCacheConfig.ARENA_CACHCE);
        Cache pdlCache = new ConcurrentMapCache(EhCacheConfig.PDL_CACHE);
        Cache norgGeoEnhetCache = new ConcurrentMapCache(EhCacheConfig.NORG_GEO_ENHET);
        Cache norgNavnCache = new ConcurrentMapCache(EhCacheConfig.NORGNAVN_CACHE);

        when(cacheManager.getCache(EhCacheConfig.ARENA_CACHCE)).thenReturn(arenaCache);
        when(cacheManager.getCache(EhCacheConfig.ARENA_CACHCE)).thenReturn(pdlCache);
        when(cacheManager.getCache(EhCacheConfig.NORG_GEO_ENHET)).thenReturn(norgGeoEnhetCache);
        when(cacheManager.getCache(EhCacheConfig.NORGNAVN_CACHE)).thenReturn(norgNavnCache);
    }


    @Test
    public void bekreft_antall_ganger_Cacheable_endepunkter_blir_kalt_ved_endreAvtale() {
        final NavEnhet geoNavEnhet = TestData.ENHET_GEOGRAFISK;
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        TestData.setGeoNavEnhet(avtale, oppfolgingNavEnhet);
        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        final TilgangskontrollService mockTilgangskontrollService = mock(TilgangskontrollService.class);
        final PersondataService mockPersondataService = mock(PersondataService.class);
        final Norg2Client mockNorg2Client = mock(Norg2Client.class);
        final VeilarbArenaClient mockVeilarbArenaClient = mock(VeilarbArenaClient.class);

        lenient().when(mockTilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(avtale.getVeilederNavIdent()),
                eq(avtale.getDeltakerFnr())
        )).thenReturn(true, true, true);

        when(mockVeilarbArenaClient.hentOppfølgingsEnhet(avtale.getDeltakerFnr().asString()))
                .thenReturn(oppfolgingNavEnhet.getVerdi(), geoNavEnhet.getVerdi(), geoNavEnhet.getVerdi());

        when(mockNorg2Client.hentGeografiskEnhet(any()))
                .thenReturn(
                        new Norg2GeoResponse(
                                oppfolgingNavEnhet.getNavn(),
                                oppfolgingNavEnhet.getVerdi()
                        ),
                        new Norg2GeoResponse(
                                geoNavEnhet.getNavn(),
                                geoNavEnhet.getVerdi()
                        ),
                        new Norg2GeoResponse(
                                geoNavEnhet.getNavn(),
                                geoNavEnhet.getVerdi()
                        )
                );


        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                mockTilgangskontrollService,
                mockPersondataService,
                mockNorg2Client,
                Set.of(new NavEnhet(avtale.getEnhetOppfolging(), avtale.getEnhetsnavnOppfolging())),
                new SlettemerkeProperties(),
                new TilskuddsperiodeConfig(),
                false,
                mockVeilarbArenaClient
        );

        // query($ident: ID!) { hentPerson(ident: $ident) { navn { fornavn mellomnavn etternavn } adressebeskyttelse { gradering } } hentGeografiskTilknytning(ident: $ident){ gtType gtKommune gtBydel gtLand regel } }

        when(mockVeilarbArenaClient.HentOppfølgingsenhetFraArena(avtale.getDeltakerFnr().asString())).thenCallRealMethod();
        when(mockNorg2Client.hentOppfølgingsEnhetsnavnFraNorg2(any())).thenCallRealMethod();
        when(mockNorg2Client.hentGeoEnhetFraNorg2(any())).thenCallRealMethod();
        when(mockPersondataService.hentPersondataFraPdl(avtale.getDeltakerFnr())).thenCallRealMethod();

        veileder.endreAvtale(
                Instant.now(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale,
                TestData.avtalerMedTilskuddsperioder
        );

        veileder.endreAvtale(
                Instant.now(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale,
                TestData.avtalerMedTilskuddsperioder
        );

    }

}
