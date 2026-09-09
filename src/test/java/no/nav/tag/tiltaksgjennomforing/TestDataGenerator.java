package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;
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
    private final static List<String> BEDRIFT_NR_FRA_WIREMOCK = List.of("999999999", "910712307", "910712314", "910712306");
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
                        currAvtale.setDeltakerFnr(genererTilfeldigGyldigFnr());
                        currAvtale.oppdaterStatus();
                        settOppfolgingKrevesForVtao(currAvtale);
                        avtaler.add(currAvtale);
                    });
        });
        return avtaler;
    }

    static void settOppfolgingKrevesForVtao(Avtale avtale) {
        settOppfolgingKrevesForVtao(avtale, Now.localDate().minusDays(1));
    }

    static void settOppfolgingKrevesForVtao(Avtale avtale, LocalDate oppfolgingFom) {
        if (
            avtale.getTiltakstype() == Tiltakstype.VTAO
                && avtale.getStatus() != Status.ANNULLERT
                && avtale.getStatus() != Status.AVSLUTTET
        ) {
            avtale.setKreverOppfolgingFom(oppfolgingFom);
            avtale.setOppfolgingVarselSendt(Now.instant());
        }
    }

    private static Fnr genererTilfeldigGyldigFnr() {
        long randomAar = Math.round(Math.random() * 32) + 18;
        long randomDag = randomAar > 19 ? Math.round(Math.random() * 365) : 0;
        return Fnr.generer(LocalDate.now().minusYears(randomAar).minusDays(randomDag));
    }

     private static String genererTilfeldigGyldigBedriftNr(){
        long tilfeldigIndex = Math.round(Math.random() * (BEDRIFT_NR_FRA_WIREMOCK.size() - 1));
        return BEDRIFT_NR_FRA_WIREMOCK.get((int) tilfeldigIndex); // Tilfeldig valg av bedriftNr
    }
}
