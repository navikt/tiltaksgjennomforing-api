package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST })
@DirtiesContext
public class AvtalestatusTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @BeforeEach
    public void setup() {
        Now.resetClock();
    }

    @Test
    public void status__ny_avtale() {
        Avtale avtale = TestData.enArbeidstreningAvtale();

        assertThat(avtale.getStatus()).isEqualTo(Status.PÅBEGYNT);

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.PÅBEGYNT);
    }

    @Test
    public void status__starter_i_morgen() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.KLAR_FOR_OPPSTART);
    }

    @Test
    public void avbryt_ufordelt_avtale_skal_bli_fordelt() {
        Avtale avtale = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));
        avtale.annuller(veileder, "grunnen");

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.ANNULLERT);
        assertThat(dbAvtale.erUfordelt()).isFalse();
        assertThat(dbAvtale.getVeilederNavIdent()).isEqualTo(veileder.getIdentifikator());
    }

    @Test
    public void avtaleklarForOppstart() {
        Avtale avtale = TestData.enAvtaleKlarForOppstart();

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.KLAR_FOR_OPPSTART);
    }

    @Test
    public void status__annullert() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.annuller(TestData.enVeileder(avtale), "grunnen");

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.ANNULLERT);
        assertThat(dbAvtale.getAnnullertTidspunkt()).isNotNull();
        assertThat(dbAvtale.getAnnullertGrunn()).isEqualTo("grunnen");
    }

    @Test
    public void status__startet_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate());
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
    }

    @Test
    public void status__avslutter_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusWeeks(4));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
    }

    @Test
    public void sommerjobb_må_være_godkjent_av_beslutter() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.MANGLER_GODKJENNING);
        assertThat(dbAvtale.getGjeldendeInnhold().getAvtaleInngått()).isNull();

        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());

        avtaleRepository.save(avtale);

        dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getGjeldendeInnhold().getAvtaleInngått()).isNotNull();
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.AVSLUTTET);
    }

    @Test
    public void status__veileder_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusDays(1).plusMonths(1).minusDays(1));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.KLAR_FOR_OPPSTART);
    }

    @Test
    public void status__klar_for_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.MANGLER_GODKJENNING);
    }

    @Test
    public void status__naar_deltaker_tlf_mangler() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusDays(1).plusMonths(1));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setDeltakerTlf(null);

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
    }

    @Test
    public void status__null_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.getGjeldendeInnhold().setStartDato(null);
        avtale.getGjeldendeInnhold().setSluttDato(null);

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.PÅBEGYNT);
    }

    @Test
    public void status__noe_fylt_ut() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(5));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusMonths(3));
        avtale.getGjeldendeInnhold().setBedriftNavn("testbedriftsnavn");

        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.PÅBEGYNT);
    }

    @Test
    public void status__avsluttet_i_gaar() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusWeeks(4).minusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtaleRepository.save(avtale);

        Avtale dbAvtale = avtaleRepository.findById(avtale.getId()).orElse(null);
        assertThat(dbAvtale.getStatus()).isEqualTo(Status.AVSLUTTET);
    }



}
