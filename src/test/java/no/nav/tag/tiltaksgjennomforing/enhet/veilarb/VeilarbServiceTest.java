package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
class VeilarbServiceTest {

    @Autowired
    private VeilarbService veilarbService;

    @BeforeEach
    void setUp() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void sjekkAt_kvalifiseringsgruppe_som_faller_utenfor_kaster_exception() {
        String fnr_har_kvalifiseringsgruppe_med_kode_IVURD = "02104317386";
        final Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setDeltakerFnr(new Fnr(fnr_har_kvalifiseringsgruppe_med_kode_IVURD));
        avtale.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        assertThatThrownBy(() -> veilarbService.hentOgSjekkOppfolgingstatus(avtale))
                .isExactlyInstanceOf(FeilkodeException.class)
                .hasMessage(Feilkode.INNSATSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL.name());
    }

    @Test
    public void hent_og_sjekk_oppfølging_status() {
        String fnr_har_riktig_kvalifisering_og_formidlingskode = "00000000000";
        final Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setDeltakerFnr(new Fnr(fnr_har_riktig_kvalifisering_og_formidlingskode));

        Oppfølgingsstatus oppfølgingsstatus = veilarbService.hentOgSjekkOppfolgingstatus(avtale);
        assertThat(oppfølgingsstatus.getFormidlingsgruppe().getKode()).isEqualTo(("ARBS"));
        assertThat(oppfølgingsstatus.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(("VARIG"));
        assertThat(oppfølgingsstatus.getOppfolgingsenhet()).isEqualTo(("0906"));
    }

    @Test
    public void sjekk_at_lonnstilskuddsprosent_blir_satt_paa_midlertidiglonnstilskudd_ved_AvtaleInnhold_constructor() {
        final Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(null);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);

        Oppfølgingsstatus oppfølgingsstatus = veilarbService.hentOgSjekkOppfolgingstatus(avtale);
        avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
        avtale.setKvalifiseringsgruppe(oppfølgingsstatus.getKvalifiseringsgruppe());
        avtale.setFormidlingsgruppe(oppfølgingsstatus.getFormidlingsgruppe());
        avtale.endreAvtale(new EndreAvtale(), Avtalerolle.VEILEDER);

        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isNotNull();
        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(60);

        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.endreAvtale(new EndreAvtale(), Avtalerolle.VEILEDER);

        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(40);
    }

    @Test
    public void hent_oppfølging_status() {
        Oppfølgingsstatus oppfølgingStatus = veilarbService.hentOppfolging(Fnr.fraDb("01056210306"));

        assertThat(oppfølgingStatus.getFormidlingsgruppe().getKode()).isEqualTo(("ARBS"));
        assertThat(oppfølgingStatus.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(("VARIG"));
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

}
