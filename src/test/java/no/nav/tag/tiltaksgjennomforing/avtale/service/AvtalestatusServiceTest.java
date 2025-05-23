package no.nav.tag.tiltaksgjennomforing.avtale.service;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingOpprettet;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhMeldingOpprettet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka
@RecordApplicationEvents
public class AvtalestatusServiceTest {

    @Autowired
    private AvtalestatusService avtalestatusService;

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Autowired
    private ApplicationEvents events;

    @Test
    public void gjor_bare_endringer_pa_avtaler_som_er_KLAR_FOR_OPPSTART_dersom_avtalen_har_startdato_bakover_i_tid() {
        Avtale klarForOppstart  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        klarForOppstart.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        klarForOppstart.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        klarForOppstart.setStatus(Status.KLAR_FOR_OPPSTART);
        avtaleRepository.save(klarForOppstart);

        Avtale gjennomføres  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        gjennomføres.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        gjennomføres.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        gjennomføres.setStatus(Status.GJENNOMFØRES);
        avtaleRepository.save(gjennomføres);

        Avtale påbegynt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        påbegynt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        påbegynt.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        påbegynt.setStatus(Status.PÅBEGYNT);
        avtaleRepository.save(påbegynt);

        Avtale manglerGodkjenning  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        manglerGodkjenning.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        manglerGodkjenning.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        manglerGodkjenning.setStatus(Status.MANGLER_GODKJENNING);
        avtaleRepository.save(manglerGodkjenning);

        Avtale annullert  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        annullert.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        annullert.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        annullert.setStatus(Status.ANNULLERT);
        avtaleRepository.save(annullert);

        Avtale avsluttet  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avsluttet.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        avsluttet.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        avsluttet.setStatus(Status.AVSLUTTET);
        avtaleRepository.save(avsluttet);

        Avtale avbrutt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avbrutt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(1));
        avbrutt.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(10));
        avbrutt.setStatus(Status.AVBRUTT);
        avtaleRepository.save(avbrutt);

        avtalestatusService.oppdaterAvtalerSomKreverEndringAvStatus();

        Status klarForOppstartStatus = avtaleRepository.findById(klarForOppstart.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(klarForOppstartStatus).isEqualTo(Status.GJENNOMFØRES);

        Status gjennomføresStatus = avtaleRepository.findById(gjennomføres.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(gjennomføresStatus).isEqualTo(Status.GJENNOMFØRES);

        Status påbegyntStatus = avtaleRepository.findById(påbegynt.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(påbegyntStatus).isEqualTo(Status.PÅBEGYNT);

        Status manglerGodkjenningStatus = avtaleRepository.findById(manglerGodkjenning.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(manglerGodkjenningStatus).isEqualTo(Status.MANGLER_GODKJENNING);

        Status annullertStatus = avtaleRepository.findById(annullert.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(annullertStatus).isEqualTo(Status.ANNULLERT);

        Status avsluttetStatus = avtaleRepository.findById(avsluttet.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(avsluttetStatus).isEqualTo(Status.AVSLUTTET);

        Status avbruttStatus = avtaleRepository.findById(avbrutt.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(avbruttStatus).isEqualTo(Status.AVBRUTT);
    }

    @Test
    public void gjor_endringer_pa_avtaler_som_GJENNOMFORES_og_KLAR_TIL_OPPSTART_dersom_start_og_sluttdato_er_bakover_i_tid() {
        Avtale klarForOppstart  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        klarForOppstart.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        klarForOppstart.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        klarForOppstart.setStatus(Status.KLAR_FOR_OPPSTART);
        avtaleRepository.save(klarForOppstart);

        Avtale gjennomføres  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        gjennomføres.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        gjennomføres.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        gjennomføres.setStatus(Status.GJENNOMFØRES);
        avtaleRepository.save(gjennomføres);

        Avtale påbegynt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        påbegynt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        påbegynt.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        påbegynt.setStatus(Status.PÅBEGYNT);
        avtaleRepository.save(påbegynt);

        Avtale manglerGodkjenning  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        manglerGodkjenning.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        manglerGodkjenning.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        manglerGodkjenning.setStatus(Status.MANGLER_GODKJENNING);
        avtaleRepository.save(manglerGodkjenning);

        Avtale annullert  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        annullert.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        annullert.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        annullert.setStatus(Status.ANNULLERT);
        avtaleRepository.save(annullert);

        Avtale avsluttet  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avsluttet.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        avsluttet.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        avsluttet.setStatus(Status.AVSLUTTET);
        avtaleRepository.save(avsluttet);

        Avtale avbrutt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avbrutt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        avbrutt.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        avbrutt.setStatus(Status.AVBRUTT);
        avtaleRepository.save(avbrutt);

        avtalestatusService.oppdaterAvtalerSomKreverEndringAvStatus();

        Status klarForOppstartStatus = avtaleRepository.findById(klarForOppstart.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(klarForOppstartStatus).isEqualTo(Status.AVSLUTTET);

        Status gjennomføresStatus = avtaleRepository.findById(gjennomføres.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(gjennomføresStatus).isEqualTo(Status.AVSLUTTET);

        Status påbegyntStatus = avtaleRepository.findById(påbegynt.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(påbegyntStatus).isEqualTo(Status.PÅBEGYNT);

        Status manglerGodkjenningStatus = avtaleRepository.findById(manglerGodkjenning.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(manglerGodkjenningStatus).isEqualTo(Status.MANGLER_GODKJENNING);

        Status annullertStatus = avtaleRepository.findById(annullert.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(annullertStatus).isEqualTo(Status.ANNULLERT);

        Status avsluttetStatus = avtaleRepository.findById(avsluttet.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(avsluttetStatus).isEqualTo(Status.AVSLUTTET);

        Status avbruttStatus = avtaleRepository.findById(avbrutt.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(avbruttStatus).isEqualTo(Status.AVBRUTT);
    }

    @Test
    public void gjor_ingen_endringer_dersom_start_og_sluttdato_er_bakover_i_tid() {
        Avtale klarForOppstart  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        klarForOppstart.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        klarForOppstart.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        klarForOppstart.setStatus(Status.KLAR_FOR_OPPSTART);
        avtaleRepository.save(klarForOppstart);

        Avtale gjennomføres  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        gjennomføres.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        gjennomføres.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        gjennomføres.setStatus(Status.GJENNOMFØRES);
        avtaleRepository.save(gjennomføres);

        Avtale påbegynt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        påbegynt.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        påbegynt.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        påbegynt.setStatus(Status.PÅBEGYNT);
        avtaleRepository.save(påbegynt);

        Avtale manglerGodkjenning  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        manglerGodkjenning.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        manglerGodkjenning.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        manglerGodkjenning.setStatus(Status.MANGLER_GODKJENNING);
        avtaleRepository.save(manglerGodkjenning);

        Avtale annullert  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        annullert.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        annullert.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        annullert.setStatus(Status.ANNULLERT);
        avtaleRepository.save(annullert);

        Avtale avsluttet  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avsluttet.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        avsluttet.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        avsluttet.setStatus(Status.AVSLUTTET);
        avtaleRepository.save(avsluttet);

        Avtale avbrutt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avbrutt.getGjeldendeInnhold().setStartDato(LocalDate.now().plusDays(1));
        avbrutt.getGjeldendeInnhold().setSluttDato(LocalDate.now().plusDays(2));
        avbrutt.setStatus(Status.AVBRUTT);
        avtaleRepository.save(avbrutt);

        avtalestatusService.oppdaterAvtalerSomKreverEndringAvStatus();

        Status klarForOppstartStatus = avtaleRepository.findById(klarForOppstart.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(klarForOppstartStatus).isEqualTo(Status.KLAR_FOR_OPPSTART);

        Status gjennomføresStatus = avtaleRepository.findById(gjennomføres.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(gjennomføresStatus).isEqualTo(Status.GJENNOMFØRES);

        Status påbegyntStatus = avtaleRepository.findById(påbegynt.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(påbegyntStatus).isEqualTo(Status.PÅBEGYNT);

        Status manglerGodkjenningStatus = avtaleRepository.findById(manglerGodkjenning.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(manglerGodkjenningStatus).isEqualTo(Status.MANGLER_GODKJENNING);

        Status annullertStatus = avtaleRepository.findById(annullert.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(annullertStatus).isEqualTo(Status.ANNULLERT);

        Status avsluttetStatus = avtaleRepository.findById(avsluttet.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(avsluttetStatus).isEqualTo(Status.AVSLUTTET);

        Status avbruttStatus = avtaleRepository.findById(avbrutt.getId()).map(Avtale::getStatus).orElse(null);
        assertThat(avbruttStatus).isEqualTo(Status.AVBRUTT);
    }

    @Test
    public void sender_avtalemelding_dersom_avtalen_endres() {
        Avtale klarForOppstart  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        klarForOppstart.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        klarForOppstart.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        klarForOppstart.setStatus(Status.KLAR_FOR_OPPSTART);
        avtaleRepository.save(klarForOppstart);

        Avtale gjennomføres  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        gjennomføres.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        gjennomføres.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        gjennomføres.setStatus(Status.GJENNOMFØRES);
        avtaleRepository.save(gjennomføres);

        Avtale påbegynt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        påbegynt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        påbegynt.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        påbegynt.setStatus(Status.PÅBEGYNT);
        avtaleRepository.save(påbegynt);

        Avtale manglerGodkjenning  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        manglerGodkjenning.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        manglerGodkjenning.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        manglerGodkjenning.setStatus(Status.MANGLER_GODKJENNING);
        avtaleRepository.save(manglerGodkjenning);

        Avtale annullert  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        annullert.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        annullert.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        annullert.setStatus(Status.ANNULLERT);
        avtaleRepository.save(annullert);

        Avtale avsluttet  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avsluttet.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        avsluttet.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        avsluttet.setStatus(Status.AVSLUTTET);
        avtaleRepository.save(avsluttet);

        Avtale avbrutt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avbrutt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        avbrutt.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        avbrutt.setStatus(Status.AVBRUTT);
        avtaleRepository.save(avbrutt);

        avtalestatusService.oppdaterAvtalerSomKreverEndringAvStatus();

        List<AvtaleMeldingOpprettet> statusendringEvents = events.stream(AvtaleMeldingOpprettet.class)
                .filter(e -> e.getEntitet().getHendelseType().equals(HendelseType.STATUSENDRING))
                .toList();

        assertThat(statusendringEvents.size()).isEqualTo(2);

        List<UUID> avtaler = statusendringEvents.stream()
            .map(avtaleMeldingOpprettet -> avtaleMeldingOpprettet.getEntitet().getAvtaleId())
            .toList();

        assertThat(avtaler).asList().contains(klarForOppstart.getId());
        assertThat(avtaler).asList().contains(gjennomføres.getId());
    }

    @Test
    public void sender_dvhmelding_dersom_avtalen_endres() {
        Avtale klarForOppstart  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        klarForOppstart.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        klarForOppstart.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        klarForOppstart.setStatus(Status.KLAR_FOR_OPPSTART);
        avtaleRepository.save(klarForOppstart);

        Avtale gjennomføres  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        gjennomføres.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        gjennomføres.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        gjennomføres.setStatus(Status.GJENNOMFØRES);
        avtaleRepository.save(gjennomføres);

        Avtale påbegynt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        påbegynt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        påbegynt.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        påbegynt.setStatus(Status.PÅBEGYNT);
        avtaleRepository.save(påbegynt);

        Avtale manglerGodkjenning  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        manglerGodkjenning.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        manglerGodkjenning.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        manglerGodkjenning.setStatus(Status.MANGLER_GODKJENNING);
        avtaleRepository.save(manglerGodkjenning);

        Avtale annullert  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        annullert.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        annullert.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        annullert.setStatus(Status.ANNULLERT);
        avtaleRepository.save(annullert);

        Avtale avsluttet  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avsluttet.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        avsluttet.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        avsluttet.setStatus(Status.AVSLUTTET);
        avtaleRepository.save(avsluttet);

        Avtale avbrutt  = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avbrutt.getGjeldendeInnhold().setStartDato(LocalDate.now().minusDays(10));
        avbrutt.getGjeldendeInnhold().setSluttDato(LocalDate.now().minusDays(1));
        avbrutt.setStatus(Status.AVBRUTT);
        avtaleRepository.save(avbrutt);

        avtalestatusService.oppdaterAvtalerSomKreverEndringAvStatus();

        List<DvhMeldingOpprettet> statusendringEvents = events.stream(DvhMeldingOpprettet.class).toList();

        assertThat(statusendringEvents.size()).isEqualTo(2);

        List<UUID> avtaler = statusendringEvents.stream()
            .map(avtaleMeldingOpprettet -> avtaleMeldingOpprettet.getEntitet().getAvtaleId())
            .toList();

        assertThat(avtaler).asList().contains(klarForOppstart.getId());
        assertThat(avtaler).asList().contains(gjennomføres.getId());
    }

}
