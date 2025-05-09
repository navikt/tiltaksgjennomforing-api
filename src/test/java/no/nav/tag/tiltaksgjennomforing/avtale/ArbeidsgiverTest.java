package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetDatoErTilbakeITidException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING);

        PersondataService persondataService = mock(PersondataService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        EregService eregService  = mock(EregService.class);

        Norg2GeoResponse navEnhet = new Norg2GeoResponse("Nav Grorud", "0411");
        when(norg2Client.hentGeografiskEnhet(any())).thenReturn(navEnhet);
        when(persondataService.hentNavn(any())).thenReturn(new Navn("Donald", "", "Duck"));
        when(persondataService.hentGeografiskTilknytning(any())).thenReturn(Optional.of("0904"));
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

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
                persondataService,
                norg2Client,
                eregService
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
                null
        );
        assertThatThrownBy(
                () -> arbeidsgiver.avvisDatoerTilbakeITid(avtale, Now.localDate(), Now.localDate().minusDays(1))
        ).isInstanceOf(VarighetDatoErTilbakeITidException.class);
    }
}
