package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;

import java.time.LocalDateTime;
import java.util.List;

public class TestData {
    static Avtale enAvtale() {
        NavIdent veilderNavIdent = new NavIdent("X123456");
        return Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
    }

    static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("12345678901");
        Fnr arbeidsgiverFnr = new Fnr("01234567890");
        return new OpprettAvtale(deltakerFnr, arbeidsgiverFnr);
    }

    static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    static EndreAvtale endringPaAlleFelt() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Fornavn");
        endreAvtale.setDeltakerEtternavn("Etternavn");
        endreAvtale.setDeltakerAdresse("Adresse");
        endreAvtale.setDeltakerPostnummer("Postnr");
        endreAvtale.setDeltakerPoststed("Poststed");
        endreAvtale.setBedriftNavn("Bedriftnavn");
        endreAvtale.setBedriftAdresse("Bedriftadresse");
        endreAvtale.setBedriftPostnummer("Bedriftspostnr");
        endreAvtale.setBedriftPoststed("Bedriftpoststed");
        endreAvtale.setArbeidsgiverFornavn("AG fornavn");
        endreAvtale.setArbeidsgiverEtternavn("AG etternavn");
        endreAvtale.setArbeidsgiverEpost("AG epost");
        endreAvtale.setArbeidsgiverTlf("AG tlf");
        endreAvtale.setVeilederFornavn("Veilederfornavn");
        endreAvtale.setVeilederEtternavn("Veilederetternavn");
        endreAvtale.setVeilederEpost("Veilederepost");
        endreAvtale.setVeilederTlf("Veiledertlf");
        endreAvtale.setOppfolging("Oppfolging");
        endreAvtale.setTilrettelegging("Tilrettelegging");
        endreAvtale.setStartDatoTidspunkt(LocalDateTime.now());
        endreAvtale.setArbeidstreningLengde(2);
        endreAvtale.setArbeidstreningStillingprosent(50);
        endreAvtale.setMaal(List.of(TestData.etMaal(), TestData.etMaal()));
        endreAvtale.setOppgaver(List.of(TestData.enOppgave(), TestData.enOppgave()));
        return endreAvtale;
    }

    public static Bruker deltaker() {
        return new Bruker("01234567890");
    }

    static Bruker deltaker(Avtale avtale) {
        return new Bruker(avtale.getDeltakerFnr());
    }

    static Bruker deltakerUtenTilgang() {
        return new Bruker("99999999999");
    }

    static Bruker arbeidsgiver() {
        return new Bruker("12345678901");
    }

    static Bruker arbeidsgiver(Avtale avtale) {
        return new Bruker(avtale.getArbeidsgiverFnr());
    }

    public static Veileder veileder() {
        return new Veileder("X123456");
    }

    static Veileder veileder(Avtale avtale) {
        return new Veileder(avtale.getVeilederNavIdent());
    }

    public static Veileder veilederUtenTilgang() {
        return new Veileder("Y999999");
    }

    static Oppgave enOppgave() {
        return new Oppgave();
    }

    static Maal etMaal() {
        return new Maal();
    }
}
