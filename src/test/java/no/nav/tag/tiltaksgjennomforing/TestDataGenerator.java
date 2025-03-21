package no.nav.tag.tiltaksgjennomforing;

import no.nav.fnrgen.FnrGen;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomHarBlittRettetAvVeileder;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjent;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomHarBlittRettetAvVeileder;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomMåFølgesOpp;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVtaoAvtaleGodkjentAvVeileder;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomHarBlittRettetAvVeileder;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp;

public class TestDataGenerator {
    public static List<Avtale> genererAvtaler(int antall){
        List<Avtale> avtaler = new ArrayList<>();
        IntStream.range(0, antall).forEach(i -> {
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
                        currAvtale.setStatus(Status.fra(currAvtale));
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
