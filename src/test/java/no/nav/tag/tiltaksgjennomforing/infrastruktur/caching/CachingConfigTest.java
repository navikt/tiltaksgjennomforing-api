package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;


import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.LokalTiltaksgjennomforingApplication;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.enhet.*;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVeileder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL,  "wiremock" })
// @AutoConfigureCache
@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = { CaffeineCachingConfig.class }, loader = AnnotationConfigContextLoader.class)
public class CachingConfigTest {


    @Autowired
    CacheManager cacheManager;

    @Autowired
    VeilarbArenaCache veilarbArenaCache;



    @Test
    public void sjekk_at_caching_fanger_opp_data() {
        final NavEnhet geoNavEnhet = TestData.ENHET_GEOGRAFISK;
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        TestData.setGeoNavEnhet(avtale, oppfolgingNavEnhet);
        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        final TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        final PersondataService persondataService = mock(PersondataService.class);
        final Norg2Client norg2Client = mock(Norg2Client.class);
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        final VeilarbArenaClient veilarbArenaClient = mock(VeilarbArenaClient.class);

        lenient().when(veilarbArenaClient.sjekkOgHentOppfølgingStatus(any()))
                .thenReturn(
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                oppfolgingNavEnhet.getVerdi()
                        ),
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                geoNavEnhet.getVerdi()
                        ),
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                geoNavEnhet.getVerdi()
                        )
                );

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(new NavEnhet(avtale.getEnhetOppfolging(), avtale.getEnhetsnavnOppfolging())),
                new SlettemerkeProperties(),
                new TilskuddsperiodeConfig(),
                false,
                veilarbArenaClient,
                veilarbArenaCache
        );


        veilarbArenaCache.hentOppfølgingsenhet(avtale);
        veilarbArenaCache.hentOppfølgingsenhet(avtale);

        /*Cache cache = cacheManager.getCache("arena");
        cache.put("00000000000", "0906");

        Assertions.assertEquals("0906", cache.get("00000000000").get(), "Cache value should be 'value'");*/

        Assertions.assertNotNull(cacheManager.getCache("arena").get(avtale));

    }

    @Test
    public void sjekk_at_caching_fanger_opp_og_returnerer_data() {
    /*    final NavEnhet geoNavEnhet = TestData.ENHET_GEOGRAFISK;
        final NavEnhet oppfolgingNavEnhet = TestData.ENHET_OPPFØLGING;
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        TestData.setGeoNavEnhet(avtale, oppfolgingNavEnhet);
        TestData.setOppfolgingNavEnhet(avtale, oppfolgingNavEnhet);

        final TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        final PersondataService persondataService = mock(PersondataService.class);
        final Norg2Client norg2Client = mock(Norg2Client.class);
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        final VeilarbArenaClient veilarbArenaClient = mock(VeilarbArenaClient.class);

        lenient().when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(avtale.getVeilederNavIdent()),
                eq(avtale.getDeltakerFnr())
        )).thenReturn(true, true, true);

        lenient().when(veilarbArenaClient.sjekkOgHentOppfølgingStatus(any()))
                .thenReturn(
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                oppfolgingNavEnhet.getVerdi()
                        ),
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                geoNavEnhet.getVerdi()
                        ),
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                geoNavEnhet.getVerdi()
                        )
                );

        when(persondataService.hentPersondata(avtale.getDeltakerFnr())).thenReturn(pdlRespons, pdlRespons, pdlRespons);
        when(persondataService.erKode6(pdlRespons)).thenCallRealMethod();

        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
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

        when(veilarbArenaClient.hentOppfølgingsEnhet(avtale.getDeltakerFnr().asString()))
                .thenReturn(oppfolgingNavEnhet.getVerdi(), geoNavEnhet.getVerdi(), geoNavEnhet.getVerdi());

        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
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
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(new NavEnhet(avtale.getEnhetOppfolging(), avtale.getEnhetsnavnOppfolging())),
                new SlettemerkeProperties(),
                new TilskuddsperiodeConfig(),
                false,
                veilarbArenaClient
        );

        TilskuddsperiodeConfig tilskuddsperiodeConfig = new TilskuddsperiodeConfig();
        tilskuddsperiodeConfig.setPilotvirksomheter(List.of());
        tilskuddsperiodeConfig.setTiltakstyper(EnumSet.of(Tiltakstype.SOMMERJOBB));


        veileder.endreAvtale(
                Instant.now(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale,
                tilskuddsperiodeConfig.getTiltakstyper(),
                tilskuddsperiodeConfig.getPilotvirksomheter(),
                List.of()
        );


        veileder.endreAvtale(
                Instant.now(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale,
                tilskuddsperiodeConfig.getTiltakstyper(),
                tilskuddsperiodeConfig.getPilotvirksomheter(),
                List.of()
        );

       Assertions.assertNotNull(cacheManager.getCache("OPPFOLGING_ENHET_CACHE").get(avtale.getDeltakerFnr()));
*/
    }
}
