package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetDatoErTilbakeITidException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;


public class ArbeidsgiverTest {
    @Test
    public void opphevGodkjenninger__kan_oppheve_ved_deltakergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        assertThatThrownBy(() -> arbeidsgiver.opphevGodkjenninger(avtale)).isInstanceOf(KanIkkeOppheveException.class);
    }

    @Test
    public void oprettAvtale__setter_startverdier_på_avtale() {
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        final NavEnhet navEnhet = TestData.ENHET_GEOGRAFISK;
        final OpprettAvtale opprettAvtale = new OpprettAvtale(
                TestData.etFodselsnummer(),
                TestData.etBedriftNr(),
                Tiltakstype.ARBEIDSTRENING
        );

        PersondataService persondataService = mock(PersondataService.class);
        final Norg2Client norg2Client = mock(Norg2Client.class);
        final VeilarbArenaClient veilarbArenaClient = mock(VeilarbArenaClient.class);


        when(persondataService.hentPersondata(TestData.etFodselsnummer())).thenReturn(pdlRespons);
        when(persondataService.erKode6(pdlRespons)).thenCallRealMethod();
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
                .thenReturn(new Norg2GeoResponse(
                        TestData.ENHET_GEOGRAFISK.getNavn(),
                        TestData.ENHET_GEOGRAFISK.getVerdi()
                ));
        when(veilarbArenaClient.hentOppfølgingsEnhet(TestData.etFodselsnummer().asString())).thenReturn(navEnhet.getVerdi());
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
                .thenReturn(new Norg2GeoResponse(
                        TestData.ENHET_GEOGRAFISK.getNavn(),
                        TestData.ENHET_GEOGRAFISK.getVerdi()
                ));

        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(new AltinnReportee(
                        "",
                        "",
                        null,
                        TestData.etBedriftNr().asString(),
                        null,
                        null
                )),
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                persondataService,
                norg2Client,
                veilarbArenaClient
        );

        Avtale avtale = arbeidsgiver.opprettAvtale(opprettAvtale);

        assertThat(avtale.isOpprettetAvArbeidsgiver()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isNull();
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
                null
        );
        assertThatThrownBy(() -> arbeidsgiver.avvisDatoerTilbakeITid(avtale, Now.localDate()
                .minusDays(1), null)).isInstanceOf(VarighetDatoErTilbakeITidException.class);
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
                null
        );
        assertThatThrownBy(() -> arbeidsgiver.avvisDatoerTilbakeITid(avtale, Now.localDate(), Now.localDate()
                .minusDays(1))).isInstanceOf(VarighetDatoErTilbakeITidException.class);
    }
}