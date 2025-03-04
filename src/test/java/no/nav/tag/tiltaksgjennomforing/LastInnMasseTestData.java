package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.fnrgen.FnrGen;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile(Miljø.MASSE_TESTDATA)
public class LastInnMasseTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final AvtaleRepository avtaleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Laster inn masse testdata");
/*
        for (int i = 0; i < 555; i++) {
            Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeilederTilbakeITid();
            avtale.getGjeldendeInnhold().setDeltakerFornavn(NavnGenerator.genererFornavn());
            avtale.getGjeldendeInnhold().setDeltakerEtternavn(NavnGenerator.genererEtternavn());
            avtale.getGjeldendeInnhold().setBedriftNavn(NavnGenerator.genererBedriftsnavn());
            avtaleRepository.save(avtale);
        }*/
        hentMyeMerAvtalerDataForLabs().forEach(avtale -> {
            avtale.setStatus(Status.fra(avtale));
            avtaleRepository.save(avtale);
        });
    }

    private List<Avtale> hentMyeMerAvtalerDataForLabs() {
        List<Avtale> avtaler = new ArrayList<>();

        IntStream.range(0, 500).forEach(i -> {
            BedriftNr bedriftNrTilfeldig = new BedriftNr(genererTilfeldigGyldigBedriftNr());
            List.of(
                    // Midlertidig Lonnstilskudd Avtale
                            enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder(),
                            enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp(),
                            enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomHarBlittRettetAvVeileder(),
                            // VTAO Avtale
                            enVtaoAvtaleGodkjentAvVeileder(),
                            enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp(),
                            enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomHarBlittRettetAvVeileder(),
                            // Varig Lonnstilskudd Avtale
                            enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjent(),
                            enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomMåFølgesOpp(),
                            enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomHarBlittRettetAvVeileder()
                    )
                    .forEach(currAvtale -> {
                        currAvtale.setId(UUID.randomUUID());
                        currAvtale.setBedriftNr(bedriftNrTilfeldig);
                        currAvtale.getGjeldendeInnhold().setId(UUID.randomUUID());
                        currAvtale.getGjeldendeInnhold().setDeltakerFornavn(NavnGenerator.genererFornavn());
                        currAvtale.getGjeldendeInnhold().setDeltakerEtternavn(NavnGenerator.genererEtternavn());
                        currAvtale.getGjeldendeInnhold().setBedriftNavn(NavnGenerator.genererBedriftsnavn());
                        currAvtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
                        currAvtale.setDeltakerFnr(new Fnr(FnrGen.singleFnr()));

                        avtaler.add(currAvtale);
                    });
        });
        return avtaler;
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
