package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AvtaleRepositoryTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Test
    public void tomAvtaleSkalKunneLagresAvRepository() {
        String deltakerFodselsnr = "12345678901";
        Avtale avtale = Avtale.nyAvtale(deltakerFodselsnr);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        assertThat(lagretAvtale).isNotNull();
    }
}
