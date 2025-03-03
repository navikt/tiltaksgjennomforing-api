package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.fnrgen.FnrGen;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({ Miljø.TESTDATA, Miljø.DEV_GCP_LABS })
public class LastInnTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final AvtaleRepository avtaleRepository;
    @Autowired
    private Environment environment;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (avtaleRepository.count() != 0) {
            return;
        }

        List<Avtale> avtaler = new ArrayList<>();

        log.info("Laster testdata");

        avtaler.add(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederUtenTilskuddsperioder());
        avtaler.add(TestData.enArbeidstreningAvtale());
        avtaler.add(TestData.enMentorAvtaleSignert());
        avtaler.add(TestData.enMentorAvtaleUsignert());
        avtaler.add(TestData.enInkluderingstilskuddAvtale());
        avtaler.add(TestData.enInkluderingstilskuddAvtaleUtfyltOgGodkjentAvArbeidsgiver());
        avtaler.add(TestData.enAvtaleMedAltUtfylt());
        avtaler.add(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder());
        avtaler.add(TestData.enAvtaleMedFlereVersjoner());
        avtaler.add(TestData.enAvtaleKlarForOppstart());
        Avtale lilly = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        lilly.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtaler.add(lilly);
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder());

        avtaler.add(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederTilbakeITid());
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        avtaler.add(TestData.enSommerjobbAvtaleGodkjentAvVeileder());
        avtaler.add(TestData.enSommerjobbAvtaleGodkjentAvBeslutter());
        avtaler.add(TestData.enSommerjobbAvtaleGodkjentAvArbeidsgiver());
        Now.resetClock();
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder());
        avtaler.add(TestData.enMentorAvtaleMedMedAltUtfylt());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet());
        avtaler.add(TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtaler.add(TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.VARIG_LONNSTILSKUDD));
        avtaler.add(TestData.enVarigLonnstilskuddAvtaleMedBehandletIArenaPerioder());
        avtaler.add(TestData.enVtaoAvtaleGodkjentAvVeileder());
        avtaler.add(TestData.enVtaoAvtaleGodkjentAvVeilederFraAnnentOmråde());
        avtaler.add(TestData.enEtterRegistrerdVtaoAvtaleGodkjentAvVeileder());
        avtaler.add(TestData.enVtaoAvtaleGodkjentAvArbeidsgiver());

        Now.fixedDate(LocalDate.of(2024, 10, 1));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 7, 23));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 6));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 5));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 4));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 3));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        List<Avtale> avtalerDataForLabs = hentMyeMerAvtalerDataForLabs(avtaler);
        avtaler.addAll(avtalerDataForLabs);
        avtaler.forEach(avtale -> {
            avtale.setStatus(Status.fra(avtale));
            avtaleRepository.save(avtale);
        });
    }

    private List<Avtale> hentMyeMerAvtalerDataForLabs(List<Avtale> avtaler) {
        List<Avtale> veldigMangeFlereAvtaler = new ArrayList<>();

        if (Arrays.stream(environment.getActiveProfiles())
                .noneMatch(profile -> profile.equals(Miljø.DEV_GCP_LABS))) {
            return Collections.emptyList();
        }

        IntStream.range(0,5000).forEach(i -> {
            BedriftNr bedriftNrTilfeldig = new BedriftNr(genererTilfeldigGyldigBedriftNr());
            String BedriftNavnTilfeldig = genererTilfedligOrd(10);
            List.of(enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder(),
                    enVtaoAvtaleGodkjentAvVeileder(),
                            enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjent())
                   .forEach( currAvtale -> {
               currAvtale.setId(UUID.randomUUID());
               currAvtale.setBedriftNr(bedriftNrTilfeldig);
               currAvtale.getGjeldendeInnhold().setBedriftNavn(BedriftNavnTilfeldig);
               currAvtale.getGjeldendeInnhold().setId(UUID.randomUUID());
               currAvtale.getGjeldendeInnhold().setDeltakerFornavn(genererTilfedligOrd(5));
               currAvtale.getGjeldendeInnhold().setDeltakerEtternavn(genererTilfedligOrd(5));
               currAvtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
               currAvtale.setDeltakerFnr(new Fnr(FnrGen.singleFnr().toString()));

               veldigMangeFlereAvtaler.add(currAvtale);
            });
        });
        return veldigMangeFlereAvtaler;
    }


    public static String genererTilfedligOrd(int length) {
        StringBuilder ord = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
                RandomStringGenerator currGenerertOrd = new RandomStringGenerator.Builder()
                        .withinRange('a', 'z')
                        .build();
            ord.append(currGenerertOrd.generate(1));
        }
        return ord.toString();
    }

    public static String genererTilfeldigGyldigBedriftNr(){
        int num1 = (int) Math.floor(Math.random()*10);
        int num2 = (int) Math.floor(Math.random()*10);
        int num3 = (int) Math.floor(Math.random()*10);
        int num4 = (int) Math.floor(Math.random()*10);
        int num5 = (int) Math.floor(Math.random()*10);
        int num6 = (int) Math.floor(Math.random()*10);
        int num7 = (int) Math.floor(Math.random()*10);
        int num8 = (int) Math.floor(Math.random()*10);

        // vekt: 3 2 7 6 5 4 3 2
        var weighted = num1*3 + num2*2 + num3*7 + num4*6 + num5*5 + num6*4 + num7*3 + num8*2;
        var remainder = weighted % 11;
        var contr = 11 - remainder;

        if (contr == 11)
            contr = 0;
        if (contr == 10)
            return null; // feil orgnr
        else
            return "" + num1 + num2 + num3 + num4 + num5 + num6 + num7 + num8 + contr; // valid orgnr
    }
}
