package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());

        List<Avtale> avtaleAlleredeRegistrertPaDeltaker = avtaleRepository.finnAvtalerSomOverlapperForDeltaker(
                "00000000000",
                UUID.randomUUID().toString(),
                LocalDate.now(),
                null
        );
        Assertions.assertEquals(3, avtaleAlleredeRegistrertPaDeltaker.size());

        List<Avtale> avtalePaDeltakerUtenNoenAvtaleId = avtaleRepository.finnAvtalerSomOverlapperForDeltaker(
                "00000000000",
                null,
                LocalDate.now(),
                null
        );
        Assertions.assertEquals(3, avtalePaDeltakerUtenNoenAvtaleId.size());
    }
}
