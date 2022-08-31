package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles({Miljø.LOCAL})
@DirtiesContext
public class DeltakerAlleredePaTiltakTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Test
    public void skal_returnere_avtaler_deltaker_allerede_er_registrert_paa() {
        avtaleRepository.save(TestData.enArbeidstreningAvtale());
        avtaleRepository.save(TestData.enMentorAvtaleSignert());
        List<Avtale> avtaleAlleredeRegistrertPaDeltaker = avtaleRepository.findAllByDeltakerFnr(new Fnr("00000000000"));

        Assertions.assertEquals(2, avtaleAlleredeRegistrertPaDeltaker.size());
    }
}
