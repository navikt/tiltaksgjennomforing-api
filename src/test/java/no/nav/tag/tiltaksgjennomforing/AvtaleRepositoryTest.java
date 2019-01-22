package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.domene.Maal;
import no.nav.tag.tiltaksgjennomforing.domene.Oppgave;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AvtaleRepositoryTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Test
    public void nyAvtaleSkalKunneLagreOgReturneresAvRepository() {
        Avtale lagretAvtale = avtaleRepository.save(TestData.enAvtale());

        Optional<Avtale> avtaleOptional = avtaleRepository.findById(lagretAvtale.getId());
        assertThat(avtaleOptional).isPresent();
    }

    @Test
    public void skalKunneLagreMaalFlereGanger() {
        // Lage avtale
        Avtale lagretAvtale = avtaleRepository.save(TestData.enAvtale());

        // Lagre maal skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        Maal maal = TestData.etMaal();
        endreAvtale.setMaal(List.of(maal));
        lagretAvtale.endreAvtale(1, TestData.veileder(), endreAvtale);
        avtaleRepository.save(lagretAvtale);

        // Lage ny avtale
        Avtale lagretAvtale2 = avtaleRepository.save(TestData.enAvtale());

        // Lagre maal skal enda fungere
        EndreAvtale endreAvtale2 = new EndreAvtale();
        Maal maal2 = TestData.etMaal();
        endreAvtale2.setMaal(List.of(maal2));
        lagretAvtale2.endreAvtale(1, TestData.veileder(), endreAvtale2);
        avtaleRepository.save(lagretAvtale2);
    }

    @Test
    public void skalKunneLagreOppgaverFlereGanger() {
        // Lage avtale
        Avtale lagretAvtale = avtaleRepository.save(TestData.enAvtale());

        // Lagre maal skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        Oppgave oppgave = TestData.enOppgave();
        endreAvtale.setOppgaver(List.of(oppgave));
        lagretAvtale.endreAvtale(1, TestData.veileder(), endreAvtale);
        avtaleRepository.save(lagretAvtale);

        // Lage ny avtale
        Avtale lagretAvtale2 = avtaleRepository.save(TestData.enAvtale());

        // Lagre maal skal enda fungere
        EndreAvtale endreAvtale2 = new EndreAvtale();
        Oppgave oppgave2 = TestData.enOppgave();
        endreAvtale2.setOppgaver(List.of(oppgave2));
        lagretAvtale2.endreAvtale(1, TestData.veileder(), endreAvtale2);
        avtaleRepository.save(lagretAvtale2);
    }
}
