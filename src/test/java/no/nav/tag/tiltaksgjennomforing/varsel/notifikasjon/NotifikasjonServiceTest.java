package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({Miljø.LOCAL, "wiremock"})
@DirtiesContext
public class NotifikasjonServiceTest {

    @Autowired
    NotifikasjonService notifikasjonService;

    @Autowired
    NotifikasjonParser parser;

    @Autowired
    ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    @Autowired
    AvtaleRepository avtaleRepository;

    Avtale avtale;
    ArbeidsgiverNotifikasjon notifikasjon;

    @Before
    public void init() {
        avtale = TestData.enArbeidstreningAvtale();
        avtaleRepository.save(avtale);
        notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(
                        avtale,
                        VarslbarHendelseType.OPPRETTET,
                        notifikasjonService,
                        parser);
    }

    @Test
    public void opprettNyBeskjedTest() {
        notifikasjonService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);

        assertThat(arbeidsgiverNotifikasjonRepository.findAllByAvtaleId(avtale.getId())).isNotEmpty();
    }

    @Test
    public void opprettNyOppgaveTest() {
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);

        assertThat(arbeidsgiverNotifikasjonRepository.
                findArbeidsgiverNotifikasjonByAvtaleIdAndVarselSendtVellykketAndNotifikasjonAktiv(
                        avtale.getId(),
                        true,
                        true))
                .isNotEmpty();
    }

    @Test
    public void findArbeidsgiverNotifikasjonByIdAndHendelseTypeAndStatusResponseTest() {
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);

        List<ArbeidsgiverNotifikasjon> notifikasjonList =
                arbeidsgiverNotifikasjonRepository.
                        findArbeidsgiverNotifikasjonByAvtaleIdAndHendelseTypeAndStatusResponse(
                        avtale.getId(),
                        this.notifikasjon.getHendelseType(),
                        MutationStatus.NY_OPPGAVE_VELLYKKET.getStatus());
        ArbeidsgiverNotifikasjon notifikasjon = notifikasjonList.get(0);

        assertThat(notifikasjon).isNotNull();
        assertThat(notifikasjon.getAvtaleId()).isEqualTo(avtale.getId());
        assertThat(notifikasjon.getHendelseType()).isEqualTo(VarslbarHendelseType.OPPRETTET);
        assertThat(notifikasjon.getStatusResponse()).isEqualTo(MutationStatus.NY_OPPGAVE_VELLYKKET.getStatus());
    }

    @Test
    public void settOppgaveUtfoertTest() {
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);

        List<ArbeidsgiverNotifikasjon> notifikasjonList =
                arbeidsgiverNotifikasjonRepository.
                        findArbeidsgiverNotifikasjonByAvtaleIdAndVarselSendtVellykketAndNotifikasjonAktiv(
                        avtale.getId(),
                                true,
                                true);
        ArbeidsgiverNotifikasjon notifikasjon = notifikasjonList.get(0);

        assertThat(notifikasjonList.get(0)).isNotNull();
        assertThat(notifikasjon.getAvtaleId()).isEqualTo(avtale.getId());
        assertThat(notifikasjon.getStatusResponse()).isEqualTo(MutationStatus.NY_OPPGAVE_VELLYKKET.getStatus());
        assertThat(notifikasjon.isNotifikasjonAktiv()).isTrue();

        notifikasjonService.oppgaveUtfoert(
                avtale,
                VarslbarHendelseType.OPPRETTET,
                MutationStatus.NY_OPPGAVE_VELLYKKET, VarslbarHendelseType.AVTALE_INNGÅTT);

        List<ArbeidsgiverNotifikasjon> oppdatertNotifikasjonList =
                arbeidsgiverNotifikasjonRepository.findAllByAvtaleId(avtale.getId());

        assertThat(oppdatertNotifikasjonList.get(0)).isNotNull();
        assertThat(oppdatertNotifikasjonList.get(0).getAvtaleId()).isEqualTo(avtale.getId());
        assertThat(oppdatertNotifikasjonList.get(0).isNotifikasjonAktiv()).isFalse();
        assertThat(oppdatertNotifikasjonList.get(1)).isNotNull();
        assertThat(oppdatertNotifikasjonList.get(1).getAvtaleId()).isEqualTo(avtale.getId());
        assertThat(oppdatertNotifikasjonList.get(1).getStatusResponse())
                .isEqualTo(MutationStatus.OPPGAVE_UTFOERT_VELLYKKET.getStatus());
    }

    @Test
    public void oppGaveUtfoertSkalKunlagresEnGangPrHendelse() {
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);

        notifikasjonService.oppgaveUtfoert(
                avtale,
                VarslbarHendelseType.OPPRETTET,
                MutationStatus.NY_OPPGAVE_VELLYKKET, VarslbarHendelseType.AVTALE_INNGÅTT);

        notifikasjonService.oppgaveUtfoert(
                avtale,
                VarslbarHendelseType.OPPRETTET,
                MutationStatus.NY_OPPGAVE_VELLYKKET, VarslbarHendelseType.AVTALE_INNGÅTT);

        List<ArbeidsgiverNotifikasjon> oppdatertNotifikasjonList =
                arbeidsgiverNotifikasjonRepository.findAllByAvtaleId(avtale.getId());

        assertThat(oppdatertNotifikasjonList.size()).isEqualTo(2);
    }

    @Test
    public void softDeleteNotifikasjonTest() {
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);
        List<ArbeidsgiverNotifikasjon> notifikasjonList =
                arbeidsgiverNotifikasjonRepository.findAllByAvtaleId(avtale.getId());
        ArbeidsgiverNotifikasjon notifikasjon = notifikasjonList.get(0);

        assertThat(notifikasjonList.get(0)).isNotNull();
        assertThat(notifikasjon.getStatusResponse()).isEqualTo(MutationStatus.NY_OPPGAVE_VELLYKKET.getStatus());
        assertThat(notifikasjon.isNotifikasjonAktiv()).isTrue();

        notifikasjonService.softDeleteNotifikasjoner(avtale);

        List<ArbeidsgiverNotifikasjon> oppdatertNotifikasjonList =
                arbeidsgiverNotifikasjonRepository.findAllByAvtaleId(avtale.getId());
        ArbeidsgiverNotifikasjon oppdatertNotifikasjon = oppdatertNotifikasjonList.get(0);

        assertThat(oppdatertNotifikasjon).isNotNull();
        assertThat(oppdatertNotifikasjon.getAvtaleId()).isEqualTo(avtale.getId());
        assertThat(oppdatertNotifikasjon.getStatusResponse())
                .isEqualTo(MutationStatus.SOFT_DELETE_NOTIFIKASJON_VELLYKKET.getStatus());
        assertThat(oppdatertNotifikasjon.isNotifikasjonAktiv()).isFalse();
    }

}
