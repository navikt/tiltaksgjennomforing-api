package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles(Miljø.TEST)
public class DeltakerTest {

    @MockBean
    private AvtaleRepository avtaleRepository;

    @Test
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        assertThatThrownBy(() -> deltaker.opphevGodkjenninger(avtale)).isInstanceOf(KanIkkeOppheveException.class);
    }

    @Test
    public void mentor_en_Avtale__skjul_mentor_fnr_for_deltaker() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        when(avtaleRepository.findById(any())).thenReturn(Optional.of(avtale));

        Avtale avtaler = deltaker.hentAvtale(avtaleRepository, avtale.getId());
        assertThat(avtaler.getMentorFnr()).isNull();
        assertThat(avtaler.getGjeldendeInnhold().getMentorTimelonn()).isNull();
    }

    @Test
    public void deltaker_alder_ikke_eldre_enn_72() {
        Now.fixedDate(LocalDate.of(2021, 1, 20));
        Avtale avtale = Avtale.opprett(new OpprettAvtale(new Fnr("30015521534"), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2027, 1, 30));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn()));

        Now.resetClock();
    }
}
