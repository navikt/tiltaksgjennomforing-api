package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.metrikker.MetrikkRegistrering;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.ArbeidsgiverNotifikasjonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles({Miljø.TEST, Miljø.WIREMOCK})
@DirtiesContext
class ArbeidsgiverOgMentorAvtalerEldreEnn12UkerIntegrasjonTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Autowired
    private TilskuddPeriodeRepository tilskuddPeriodeRepository;

    @Autowired
    private VarselRepository varselRepository;

    @Autowired
    private SmsRepository smsRepository;

    @Autowired
    private AvtaleInnholdRepository avtaleInnholdRepository;

    @MockBean
    private DvhMeldingEntitetRepository dvhMeldingEntitetRepository;

    @MockBean
    private AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;

    @MockBean
    private ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    @MockBean
    private MetrikkRegistrering metrikkRegistrering;

    @BeforeEach
    public void setup() {
        varselRepository.deleteAll();
        smsRepository.deleteAll();
        avtaleInnholdRepository.deleteAll();
        arbeidsgiverNotifikasjonRepository.deleteAll();
        dvhMeldingEntitetRepository.deleteAll();
        avtaleMeldingEntitetRepository.deleteAll();
        avtaleRepository.deleteAll();
        tilskuddPeriodeRepository.deleteAll();
    }

    // ARBEIDSGIVER //

    @Test
    public void skal_returnere_avtale_som_er_i_db() {
        Avtale avtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        List<Avtale> arbeidsgiverList = arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(
            avtaleRepository,
            avtale.getBedriftNr()
        );
        assertThat(arbeidsgiverList).isNotEmpty();
    }

    @Test
    @Transactional
    public void skal_IKKE_returnere_en_gammel_avtale_som_er_eldre_enn_12_uker_fra_db() {
        Avtale avtale = TestData.enArbeidstreningAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusMonths(4));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusWeeks(13));// ELDRE enn 12 UKER
        avtale.godkjennForDeltaker(avtale.getDeltakerFnr());
        avtale.godkjennForArbeidsgiver(avtale.getBedriftNr());
        avtale.godkjennForVeileder(avtale.getVeilederNavIdent());
        Avtale avtaleLagret = avtaleRepository.save(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtaleLagret);

        AvtaleQueryParameter queryParameter = new AvtaleQueryParameter();
        Pageable pageable = Pageable.ofSize(10);
        Page<Avtale> avtalerPagable = arbeidsgiver.hentAvtalerMedLesetilgang(
            avtaleRepository,
            queryParameter,
            pageable
        );
        List<Avtale> avtalerFiltrertBasertPaaRiktigTilgang = arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(
            avtaleRepository,
            avtaleLagret.getBedriftNr()
        );
        assertThat(avtalerPagable.getTotalElements()).isEqualTo(0);
        assertThat(avtalerFiltrertBasertPaaRiktigTilgang.size()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void hentBegrensedeAvtalerMedLesetilgang_skal_IKKE_returnere_en_gammel_avtale_som_er_eldre_enn_12_uker_fra_db() {
        Avtale avtale = TestData.enArbeidstreningAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusMonths(4));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusWeeks(13)); // ELDRE enn 12 UKER
        avtale.godkjennForDeltaker(avtale.getDeltakerFnr());
        avtale.godkjennForArbeidsgiver(avtale.getBedriftNr());
        avtale.godkjennForVeileder(avtale.getVeilederNavIdent());
        Avtale avtaleLagret = avtaleRepository.save(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtaleLagret);

        AvtaleQueryParameter queryParameter = new AvtaleQueryParameter();
        Pageable pageable = Pageable.ofSize(10);
        Page<BegrensetAvtale> avtalerPagable = arbeidsgiver.hentBegrensedeAvtalerMedLesetilgang(
            avtaleRepository,
            queryParameter,
            pageable
        );
        assertThat(avtalerPagable.getTotalElements()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void hentBegrensedeAvtalerMedLesetilgang_skal_returnere_en_gammel_avtale_som_er_IKKE_eldre_enn_12_uker_fra_db() {
        Avtale avtale = TestData.enArbeidstreningAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate());
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusMonths(2)); // NYERE enn 12 UKER
        avtale.godkjennForDeltaker(avtale.getDeltakerFnr());
        avtale.godkjennForArbeidsgiver(avtale.getBedriftNr());
        avtale.godkjennForVeileder(avtale.getVeilederNavIdent());
        Avtale avtaleLagret = avtaleRepository.save(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtaleLagret);

        AvtaleQueryParameter queryParameter = new AvtaleQueryParameter();
        Pageable pageable = Pageable.ofSize(10);
        Page<BegrensetAvtale> avtalerPagable = arbeidsgiver.hentBegrensedeAvtalerMedLesetilgang(
            avtaleRepository,
            queryParameter,
            pageable
        );
        assertThat(avtalerPagable.getTotalElements()).isEqualTo(1);
    }

    // MENTOR //

    @Test
    public void mentor_hentAlleAvtalerMedMuligTilgang_skal_IKKE_returnere_en_gammel_avtale_som_er_eldre_enn_12_uker_fra_db() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusMonths(3));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusWeeks(13));// ELDRE enn 12 UKER fra idag
        Avtale avtaleLagret = avtaleRepository.save(avtale);
        Mentor mentor = TestData.enMentor(avtaleLagret);

        AvtaleQueryParameter queryParameter = new AvtaleQueryParameter();
        Pageable pageable = Pageable.ofSize(10);
        Page<Avtale> avtalerPagable = mentor.hentAlleAvtalerMedMuligTilgang(
            avtaleRepository,
            queryParameter,
            pageable
        );
        assertThat(avtalerPagable.getContent()).hasSize(0);
    }


    @Test
    public void mentor_hentAlleAvtalerMedMuligTilgang_skal_returnere_en_avtale_som_er_IKKE_eldre_enn_12_uker_fra_db() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusMonths(3));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusWeeks(1));// nyere enn 12 UKER fra idag
        Avtale avtaleLagret = avtaleRepository.save(avtale);
        Mentor mentor = TestData.enMentor(avtaleLagret);

        AvtaleQueryParameter queryParameter = new AvtaleQueryParameter();
        Pageable pageable = Pageable.ofSize(10);
        Page<Avtale> avtalerPagable = mentor.hentAlleAvtalerMedMuligTilgang(
            avtaleRepository,
            queryParameter,
            pageable
        );
        assertThat(avtalerPagable.getContent()).hasSize(1);
    }
}