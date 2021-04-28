package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("testdata")
public class LastInnTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final AvtaleRepository avtaleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Laster testdata");
        avtaleRepository.save(TestData.enArbeidstreningAvtale());
        avtaleRepository.save(TestData.enAvtaleMedAltUtfylt());
        avtaleRepository.save(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder());
        avtaleRepository.save(TestData.enAvtaleMedFlereVersjoner());
        avtaleRepository.save(TestData.enAvtaleKlarForOppstart());
        Avtale lilly = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        lilly.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtaleRepository.save(lilly);
        avtaleRepository.save(TestData.enLonnstilskuddAvtaleGodkjentAvVeileder());
        avtaleRepository.save(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederTilbakeITid());
        avtaleRepository.save(TestData.enSommerjobbAvtaleGodkjentAvVeileder());
        avtaleRepository.save(TestData.enMentorAvtaleMedMedAltUtfylt());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet());
    }
}
