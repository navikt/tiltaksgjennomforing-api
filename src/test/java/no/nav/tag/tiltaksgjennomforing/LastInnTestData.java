package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("testdata")
public class LastInnTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final AvtaleRepository avtaleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Laster testdata");
        avtaleRepository.save(TestData.enAvtale());
        avtaleRepository.save(TestData.enAvtaleMedAltUtfylt());
        avtaleRepository.save(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder());
        avtaleRepository.save(TestData.enAvtaleMedFlereVersjoner());
        avtaleRepository.save(TestData.enAvtaleKlarForOppstart());
        avtaleRepository.save(TestData.enLonnstilskuddAvtaleMedMedAltUtfylt());
        avtaleRepository.save(TestData.enMentorAvtaleMedMedAltUtfylt());
    }
}
