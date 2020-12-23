package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ArbeidsgiverTest {
    @Test
    public void opphevGodkjenninger__kan_oppheve_ved_deltakergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
    }

    @Test(expected = KanIkkeOppheveException.class)
    public void opphevGodkjenninger__kan_ikke_oppheve_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger(avtale);
    }

    @Test
    public void oprettAvtale__setter_startverdier_på_avtale() {
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING);

        PersondataService persondataService = mock(PersondataService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        final String navEnhet = "0411";

        when(persondataService.hentPersondata(TestData.etFodselsnummer())).thenReturn(pdlRespons);
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel())).thenReturn(navEnhet);

        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(new AltinnReportee("", "", null, TestData.etBedriftNr().asString(), null, null)),
                Map.of(TestData.etBedriftNr(), Set.of(Tiltakstype.ARBEIDSTRENING)),
                persondataService,
                norg2Client);

        Avtale avtale = arbeidsgiver.opprettAvtale(opprettAvtale);
        assertThat(avtale.isOpprettetAvArbeidsgiver()).isTrue();
        assertThat(avtale.getDeltakerFornavn()).isNull();
        assertThat(avtale.getDeltakerEtternavn()).isNull();
        assertThat(avtale.getEnhetGeografisk()).isEqualTo(navEnhet);
    }
}