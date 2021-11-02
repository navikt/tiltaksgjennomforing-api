package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(Miljø.LOCAL)
@DirtiesContext
public class SmsVarselRepositoryTest {
    @Autowired
    private SmsVarselRepository repository;
    @Autowired
    private AvtaleRepository avtaleRepository;
    @Autowired
    private VarslbarHendelseRepository varslbarHendelseRepository;
    private Avtale avtale;
    private VarslbarHendelse varslbarHendelse;

    @BeforeEach
    public void setUp() {
        avtale = TestData.enArbeidstreningAvtale();
        avtaleRepository.save(avtale);
        varslbarHendelse = TestData.enHendelse(avtale);
        varslbarHendelseRepository.save(varslbarHendelse);
    }

    @Test
    public void save__lagrer_riktig() {
        SmsVarsel smsVarsel = SmsVarsel.nyttVarsel("00000000", TestData.enIdentifikator(), "mld", varslbarHendelse.getId());
        SmsVarsel lagretSmsVarsel = repository.save(smsVarsel);
        assertThat(lagretSmsVarsel).isEqualToIgnoringNullFields(smsVarsel);
    }
}