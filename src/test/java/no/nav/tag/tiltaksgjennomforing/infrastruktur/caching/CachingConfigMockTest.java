package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;


import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2EnhetStatus;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataClient;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.HentGeografiskTilknytning;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.HentPerson;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.Navn;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.AopTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("cache-test")
@ContextConfiguration
@ExtendWith(SpringExtension.class)
public class CachingConfigMockTest {

    private TilgangskontrollService mockTilgangskontrollService;
    private PersondataClient mockPersondataClient;
    private Norg2Client mockNorg2Client;
    private VeilarboppfolgingService mockVeilarboppfolgingService;

    @Autowired
    private TilgangskontrollService tilgangskontrollService;
    @Autowired
    private PersondataClient persondataClient;
    @Autowired
    private Norg2Client norg2Client;
    @Autowired
    private VeilarboppfolgingService veilarboppfolgingService;

    private FeatureToggleService mockFeatureToggleService;

    private Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
    private final PdlRespons FØRSTE_PDL_RESPONSE = TestData.enPdlrespons(false);
    private PdlRespons ANDRE_PDL_RESPONSE = new PdlRespons(
            new PdlRespons.Data(
                new HentPerson(null, null, List.of(new Navn("ola", "", "Norman"))),
                null,
                new HentGeografiskTilknytning("", "030202", "", "3")
            )
    );
    private final Norg2OppfølgingResponse FØRSTE_NORG2_OPPFØLGNING_RESPONSE = new Norg2OppfølgingResponse(
            1,
            "1000",
            "NAV Agder",
            Norg2EnhetStatus.AKTIV
    );
    private final Norg2OppfølgingResponse ANDRE_NORG2_OPPFØLGNING_RESPONSE = new Norg2OppfølgingResponse(
            2,
            "1001",
            "NAV Agder2",
            Norg2EnhetStatus.AKTIV
    );
    private final Norg2GeoResponse FØRSTE_NORG2_GEO_RESPONSE = new Norg2GeoResponse(
            "NAV St. Hanshaugen",
            "0313"
    );
    private final Norg2GeoResponse ANDRE_NORG2_GEO_RESPONSE = new Norg2GeoResponse(
            "NAV St. Hanshaugen2",
            "0314"
    );
    private final Oppfølgingsstatus FØRSTE_OPPFØLGNING_ENHET_ARENA = new Oppfølgingsstatus(
            Formidlingsgruppe.ARBEIDSSOKER,
            Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS,
            "0906"
    );
    private final Oppfølgingsstatus ANDRE_OPPFØLGNING_ENHET_ARENA = new Oppfølgingsstatus(
            Formidlingsgruppe.ARBEIDSSOKER,
            Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
            "0904"
    );


    @EnableCaching
    @Configuration
    @Profile("cache-test")
    public static class CachingTestConfig {

        @Bean
        public TilgangskontrollService tilgangskontrollServiceMockImplementation() {
            return mock(TilgangskontrollService.class);
        }

        @Bean
        public PersondataClient persondataClientMockImplementation() {
            return mock(PersondataClient.class);
        }

        @Bean
        public Norg2Client norg2ClientMockImplementation() {
            return mock(Norg2Client.class);
        }

        @Bean
        public VeilarboppfolgingService veilarbArenaClientMockImplementation() {
            return mock(VeilarboppfolgingService.class);
        }

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    CacheConfig.VEILARBOPPFOLGING_CACHE,
                    CacheConfig.PDL_CACHE,
                    CacheConfig.NORGNAVN_CACHE,
                    CacheConfig.NORG_GEO_ENHET,
                    CacheConfig.AXSYS_CACHE
            );
        }
    }

    @BeforeEach
    public void setUp() {
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;

        TestData.setGeoNavEnhet(avtale, oppfolgingNavEnhet);
        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        mockTilgangskontrollService = AopTestUtils.getTargetObject(tilgangskontrollService);
        mockPersondataClient = AopTestUtils.getTargetObject(persondataClient);
        mockNorg2Client = AopTestUtils.getTargetObject(norg2Client);
        mockVeilarboppfolgingService = AopTestUtils.getTargetObject(veilarboppfolgingService);

        lenient().when(mockTilgangskontrollService.harSkrivetilgangTilKandidat(
                any(),
                eq(avtale.getDeltakerFnr())
        )).thenReturn(true, true, true);

        when(mockPersondataClient.hentPersondata(avtale.getDeltakerFnr())).thenReturn(FØRSTE_PDL_RESPONSE, ANDRE_PDL_RESPONSE);
        when(mockNorg2Client.hentOppfølgingsEnhetFraCacheNorg2(any())).thenReturn(
                FØRSTE_NORG2_OPPFØLGNING_RESPONSE,
                ANDRE_NORG2_OPPFØLGNING_RESPONSE
        );
        when(mockNorg2Client.hentGeoEnhetFraCacheEllerNorg2(any())).thenReturn(FØRSTE_NORG2_GEO_RESPONSE, ANDRE_NORG2_GEO_RESPONSE);
        when(mockVeilarboppfolgingService.hentOppfolgingsstatus(avtale.getDeltakerFnr().asString())).thenReturn(
                FØRSTE_OPPFØLGNING_ENHET_ARENA,
                ANDRE_OPPFØLGNING_ENHET_ARENA
        );
    }

    @Test
    public void bekreft_antall_ganger_Cacheable_endepunkter_blir_kalt_ved_norg2Client_oppfølgingsEnhetsnavn() {
        Norg2OppfølgingResponse norg2OppfølgingResponse = norg2Client.hentOppfølgingsEnhetFraCacheNorg2(
                avtale.getEnhetOppfolging()
        );
        Norg2OppfølgingResponse norg2OppfølgingResponse2 = norg2Client.hentOppfølgingsEnhetFraCacheNorg2(
                avtale.getEnhetOppfolging()
        );

        Assertions.assertEquals(FØRSTE_NORG2_OPPFØLGNING_RESPONSE.getEnhetId(), norg2OppfølgingResponse.getEnhetId());
        Assertions.assertEquals(FØRSTE_NORG2_OPPFØLGNING_RESPONSE.getEnhetNr(), norg2OppfølgingResponse.getEnhetNr());
        Assertions.assertEquals(FØRSTE_NORG2_OPPFØLGNING_RESPONSE.getNavn(), norg2OppfølgingResponse.getNavn());

        Assertions.assertEquals(FØRSTE_NORG2_OPPFØLGNING_RESPONSE.getEnhetId(), norg2OppfølgingResponse2.getEnhetId());
        Assertions.assertEquals(FØRSTE_NORG2_OPPFØLGNING_RESPONSE.getEnhetNr(), norg2OppfølgingResponse2.getEnhetNr());
        Assertions.assertEquals(FØRSTE_NORG2_OPPFØLGNING_RESPONSE.getNavn(), norg2OppfølgingResponse2.getNavn());

        /** Blir kalt 2 ganger. Andre iterasjon så treffer vi cache response istedenfor endepunkt */
        verify(mockNorg2Client, times(1)).hentOppfølgingsEnhetFraCacheNorg2(avtale.getEnhetOppfolging());
    }

    @Test
    public void bekreft_antall_ganger_Cacheable_endepunkter_blir_kalt_ved_norg2Client_geoEnhet() {
        Optional<String> optionalGeoEnhet = FØRSTE_PDL_RESPONSE.utledGeoLokasjon();
        String geoEnhet = optionalGeoEnhet.get();

        Norg2GeoResponse norg2GeoResponse = norg2Client.hentGeoEnhetFraCacheEllerNorg2(geoEnhet);
        Norg2GeoResponse norg2GeoResponse2 = norg2Client.hentGeoEnhetFraCacheEllerNorg2(geoEnhet);

        Assertions.assertEquals(FØRSTE_NORG2_GEO_RESPONSE.getNavn(), norg2GeoResponse.getNavn());
        Assertions.assertEquals(FØRSTE_NORG2_GEO_RESPONSE.getEnhetNr(), norg2GeoResponse.getEnhetNr());

        Assertions.assertEquals(FØRSTE_NORG2_GEO_RESPONSE.getNavn(), norg2GeoResponse2.getNavn());
        Assertions.assertEquals(FØRSTE_NORG2_GEO_RESPONSE.getEnhetNr(), norg2GeoResponse2.getEnhetNr());

        /** Blir kalt 2 ganger. Andre iterasjon så treffer vi cache response istedenfor endepunkt */
        verify(mockNorg2Client, times(1)).hentGeoEnhetFraCacheEllerNorg2(geoEnhet);
    }

    @Test
    public void bekreft_antall_ganger_Cacheable_endepunkter_blir_kalt_ved_pdl() {
        PdlRespons pdlRespons = persondataClient.hentPersondata(avtale.getDeltakerFnr());
        PdlRespons pdlRespons2 = persondataClient.hentPersondata(avtale.getDeltakerFnr());

        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.utledGeoLokasjon().get(),
                pdlRespons.utledGeoLokasjon().get()
        );
        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.utledGeoLokasjon().get(),
                pdlRespons2.utledGeoLokasjon().get()
        );


        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.data().hentGeografiskTilknytning().regel(),
                pdlRespons.data().hentGeografiskTilknytning().regel()
        );
        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.data().hentGeografiskTilknytning().regel(),
                pdlRespons2.data().hentGeografiskTilknytning().regel()
        );

        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.utledNavnEllerTomtNavn().fornavn(),
                pdlRespons.utledNavnEllerTomtNavn().fornavn()
        );
        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.utledNavnEllerTomtNavn().fornavn(),
                pdlRespons2.utledNavnEllerTomtNavn().fornavn()
        );

        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.utledNavnEllerTomtNavn().etternavn(),
                pdlRespons.utledNavnEllerTomtNavn().etternavn()
        );
        Assertions.assertEquals(
                FØRSTE_PDL_RESPONSE.utledNavnEllerTomtNavn().etternavn(),
                pdlRespons2.utledNavnEllerTomtNavn().etternavn()
        );

        /** Blir kalt 2 ganger. Andre iterasjon så treffer vi cache response istedenfor endepunkt */
        verify(mockPersondataClient, times(1)).hentPersondata(avtale.getDeltakerFnr());
    }

    @Test
    public void bekreft_antall_ganger_Cacheable_endepunkter_blir_kalt_ved_endreAvtale() {
        Optional<String> optionalGeoEnhet = FØRSTE_PDL_RESPONSE.utledGeoLokasjon();
        String geoEnhet = optionalGeoEnhet.get();

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                new PersondataService(persondataClient),
                norg2Client,
                Set.of(new NavEnhet(avtale.getEnhetOppfolging(), avtale.getEnhetsnavnOppfolging())),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                mockFeatureToggleService
        );

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

        /** Blir kalt 2 ganger. Andre iterasjon så treffer vi cache response istedenfor endepunkt */
        verify(mockNorg2Client, times(1)).hentOppfølgingsEnhetFraCacheNorg2(avtale.getEnhetOppfolging());
        verify(mockNorg2Client, times(1)).hentGeoEnhetFraCacheEllerNorg2(geoEnhet);
        verify(mockPersondataClient, times(1)).hentPersondata(avtale.getDeltakerFnr());
    }
}
