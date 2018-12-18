package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AvtaleRepositoryTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Test
    public void nyAvtaleSkalKunneLagreOgReturneresAvRepository() {
        Fnr deltakerFnr = new Fnr("12345678901");
        Avtale lagretAvtale = avtaleRepository.save(Avtale.nyAvtale(deltakerFnr));

        Optional<Avtale> avtaleOptional = avtaleRepository.findById(lagretAvtale.getId());
        assertThat(avtaleOptional).isPresent();
    }
}
