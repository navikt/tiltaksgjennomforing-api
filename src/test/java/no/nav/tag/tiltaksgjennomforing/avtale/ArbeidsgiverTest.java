package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetDatoErTilbakeITidException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ArbeidsgiverTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void opphevGodkjenninger__kan_oppheve_ved_deltakergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.instant());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        assertThatThrownBy(() -> arbeidsgiver.opphevGodkjenninger(avtale)).isInstanceOf(KanIkkeOppheveException.class);
    }

    @Test
    public void oprettAvtale__setter_startverdier_på_avtale() {
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING);

        PersondataService persondataService = mock(PersondataService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        EregService eregService  = mock(EregService.class);
        VeilarboppfolgingService veilarboppfolgingService  = mock(VeilarboppfolgingService.class);

        Norg2GeoResponse navEnhet = new Norg2GeoResponse("Nav Grorud", "0411");
        when(norg2Client.hentGeografiskEnhet(any())).thenReturn(navEnhet);
        when(persondataService.hentNavn(any())).thenReturn(new Navn("Donald", "", "Duck"));
        when(persondataService.hentGeografiskTilknytning(any())).thenReturn(Optional.of("0904"));
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));
        when(veilarboppfolgingService.hentOppfolgingsstatus(any(Fnr.class))).thenReturn(new Oppfølgingsstatus(null, null, "0411"));

        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(
                    new AltinnReportee(
                        "",
                        "",
                        null,
                        TestData.etBedriftNr().asString(),
                        null,
                        null,
                        null
                    )
                ),
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                List.of(),
                persondataService,
                norg2Client,
                eregService,
                veilarboppfolgingService
        );

        Avtale avtale = arbeidsgiver.opprettAvtale(opprettAvtale);
        assertThat(avtale.getOpphav()).isEqualTo(Avtaleopphav.ARBEIDSGIVER);
        assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isNotNull();
        assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isNotNull();
        assertThat(avtale.getEnhetGeografisk()).isEqualTo(navEnhet.getEnhetNr());
    }

    @Test
    public void endreAvtale_validererFraDato() {
        Avtale avtale = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertThatThrownBy(
                () -> arbeidsgiver.avvisDatoerTilbakeITid(avtale, Now.localDate().minusDays(1), null)
        ).isInstanceOf(VarighetDatoErTilbakeITidException.class);
    }

    @Test
    public void endreAvtale_validererTilDato() {
        Avtale avtale = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertThatThrownBy(
                () -> arbeidsgiver.avvisDatoerTilbakeITid(avtale, Now.localDate(), Now.localDate().minusDays(1))
        ).isInstanceOf(VarighetDatoErTilbakeITidException.class);
    }

    @Test
    public void opprettAvtale__feilmelding_ved_diskresjonskode_og_ikke_adressesperre_tilgang() {
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                null,
                null,
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                List.of(),
                persondataService,
                null,
                null,
                null
        );
        OpprettAvtale opprettAvtale = new OpprettAvtale(new Fnr("12345678910"), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING);
        assertFeilkode(Feilkode.IKKE_TILGANG_TIL_DELTAKER_ARBEIDSGIVER, () -> arbeidsgiver.opprettAvtale(opprettAvtale));
    }

    @Test
    public void opprettAvtale__ingen_feilmelding_ved_diskresjonskode_og_ikke_adressesperre_tilgang() {
        // PDL Adressepserre og navn
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        when(persondataService.hentNavn(any())).thenReturn(new Navn("Donald", "", "Duck"));

        Norg2Client norg2Client = mock(Norg2Client.class);

        EregService eregService  = mock(EregService.class);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        VeilarboppfolgingService veilarboppfolgingService  = mock(VeilarboppfolgingService.class);
        when(veilarboppfolgingService.hentOppfolgingsstatus(any(Fnr.class))).thenReturn(new Oppfølgingsstatus(null, null, "0411"));

        List<BedriftNr> adressesperreTilganger = List.of(TestData.etBedriftNr());
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                null,
                null,
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                adressesperreTilganger,
                persondataService,
                norg2Client,
                eregService,
            veilarboppfolgingService
        );
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING);
        arbeidsgiver.opprettAvtale(opprettAvtale);
    }

    @Test
    public void hentAvtale__feilmelding_uten_adressesperre_tilgang() {
        // Repository mock
        AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setBedriftNr(TestData.etBedriftNr());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));

        // PDL Adressepserre og navn mock
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        when(persondataService.hentDiskresjonskoder(any())).thenReturn(Map.of(avtale.getDeltakerFnr(), Diskresjonskode.STRENGT_FORTROLIG));

        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                null,
                null,
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                emptyList(),
                persondataService,
                null,
                null,
                null
        );
        assertFeilkode(Feilkode.IKKE_TILGANG_TIL_AVTALE, () -> arbeidsgiver.hentAvtale(avtaleRepository, avtale.getId()));
    }

    @Test
    public void hentAvtale__ikke_feilmelding_med_adressesperre_tilgang() {
        // PDL Adressepserre og navn mock
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        // Repository mock
        AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setBedriftNr(TestData.etBedriftNr());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));


        List<BedriftNr> adressesperreTilganger = List.of(TestData.etBedriftNr());
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                null,
                null,
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                adressesperreTilganger,
                persondataService,
                null,
                null,
                null
        );
        arbeidsgiver.hentAvtale(avtaleRepository, avtale.getId());
    }

    @Test
    public void hentAlleAvterMedMuligTilgang__får_ikke_adressesperre_avtaler() {
        // Repository mock
        AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setBedriftNr(TestData.etBedriftNr());

        when(avtaleRepository.findAllByBedriftNr(any(), any(), any())).thenReturn(new PageImpl<>(List.of(avtale)));

        // PDL Adressepserre og navn mock
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskoder(any())).thenReturn(Map.of(avtale.getDeltakerFnr(), Diskresjonskode.STRENGT_FORTROLIG));


        List<BedriftNr> adressesperreTilganger = List.of(TestData.etBedriftNr());
        Arbeidsgiver arbeidsgiverUtenAdressesperreTilgang = new Arbeidsgiver(
                null,
                null,
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                emptyList(),
                persondataService,
                null,
                null,
                null
        );
        Arbeidsgiver arbeidsgiverMedAdressesperreTilgang = new Arbeidsgiver(
                null,
                null,
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                adressesperreTilganger,
                persondataService,
                null,
                null,
                null
        );
        Page<BegrensetAvtale> begrensetAvtales = arbeidsgiverUtenAdressesperreTilgang.hentBegrensedeAvtalerMedLesetilgang(avtaleRepository, new AvtaleQueryParameter(), PageRequest.of(0, 100));
        Page<BegrensetAvtale> begrensetAvtales2 = arbeidsgiverMedAdressesperreTilgang.hentBegrensedeAvtalerMedLesetilgang(avtaleRepository, new AvtaleQueryParameter(), PageRequest.of(0, 100));
        assertThat(begrensetAvtales.getContent().size()).isEqualTo(0);
        assertThat(begrensetAvtales2.getContent().size()).isEqualTo(1);
    }

}
