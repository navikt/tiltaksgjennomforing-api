package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.InnsatsgruppeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
class VeilarbServiceTest {

    @Autowired
    private VeilarbService veilarbService;

    @Test
    public void sjekkAt_kvalifiseringsgruppe_som_faller_utenfor_kaster_exception() {
        String fnr_har_kvalifiseringsgruppe_med_kode_IVURD = "10526244570";
        final Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setDeltakerFnr(new Fnr(fnr_har_kvalifiseringsgruppe_med_kode_IVURD));
        avtale.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        assertThatThrownBy(() -> veilarbService.hentOgSjekkOppfolgingstatus(avtale))
                .isExactlyInstanceOf(InnsatsgruppeException.class)
                .hasMessage(Feilkode.INNSATSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL.name());
    }

    @Test
    public void hent_og_sjekk_oppfølging_status() {
        String fnr_har_riktig_kvalifisering_og_formidlingskode = "00000000000";
        final Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setDeltakerFnr(new Fnr(fnr_har_riktig_kvalifisering_og_formidlingskode));

        Oppfølgingsstatus oppfølgingsstatus = veilarbService.hentOgSjekkOppfolgingstatus(avtale);
        assertThat(oppfølgingsstatus.getOppfolgingsenhet()).isEqualTo(("0906"));
    }

    @Test
    public void sjekk_at_lonnstilskuddsprosent_blir_satt_paa_midlertidiglonnstilskudd_ved_AvtaleInnhold_constructor() {
        final Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        Oppfølgingsstatus oppfølgingsstatus = veilarbService.hentOgSjekkOppfolgingstatus(avtale);
        avtale.setInnsatsgruppe(oppfølgingsstatus.getInnsatsgruppe());
        avtale.endreAvtale(new EndreAvtale(), Avtalerolle.VEILEDER);

        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isNotNull();
        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(60);

        avtale.setInnsatsgruppe(Innsatsgruppe.TRENGER_VEILEDNING);
        avtale.endreAvtale(new EndreAvtale(), Avtalerolle.VEILEDER);

        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(40);
    }

    @Test
    public void hent_oppfølging_status() {
        Oppfølgingsstatus oppfølgingStatus = veilarbService.hentOppfolging(Fnr.fraDb("24469412950"));
        assertThat(oppfølgingStatus.getOppfolgingsenhet()).isEqualTo(("0906"));
    }

    @Test
    public void hent_gjeldende_14a_vedtak() {
        Oppfølgingsstatus oppfølgingsstatus = veilarbService.hentOppfolging(Fnr.generer(1990, 4, 12));
        assertThat(oppfølgingsstatus.getInnsatsgruppe()).isEqualTo(Innsatsgruppe.TRENGER_VEILEDNING);
    }

    @Test
    public void kaster_exception_naar_person_ikke_har_gjeldende_vedtak() {
        Fnr fnr = Fnr.generer(1985, 3, 22);
        assertThatThrownBy(() -> veilarbService.hentOppfolging(fnr))
                .isExactlyInstanceOf(FeilkodeException.class)
                .hasMessage(Feilkode.FANT_IKKE_INNSATSBEHOV.name());
    }

    @Test
    public void kaster_enhet_mangler_naar_oppfolgingsenhet_ikke_kommer_fra_arena() {
        VeilarboppfolgingClient oppfolgingClient = mock(VeilarboppfolgingClient.class);
        VeilarbvedtaksstotteClient vedtaksstotteClient = mock(VeilarbvedtaksstotteClient.class);
        when(vedtaksstotteClient.hentGjeldende14aVedtak(any()))
                .thenReturn(Optional.of(new Gjeldende14aVedtakRespons(Innsatsgruppe.GODE_MULIGHETER, null, null)));
        when(oppfolgingClient.hentOppfolgingsenhet(any()))
                .thenReturn(Optional.of(new Oppfolgingsenhet("0301", "Nav Oslo", "NORG")));

        VeilarbService service = new VeilarbService(oppfolgingClient, vedtaksstotteClient);

        assertThatThrownBy(() -> service.hentOppfolging(Fnr.generer(1990, 4, 12)))
                .isExactlyInstanceOf(FeilkodeException.class)
                .hasMessage(Feilkode.ENHET_MANGLER.name());
    }

}
