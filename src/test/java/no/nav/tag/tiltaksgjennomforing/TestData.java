package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import no.nav.tag.tiltaksgjennomforing.varsel.BjelleVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestData {
    public static Avtale enAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
    }

    public static Avtale enAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
        avtale.endreAvtale(avtale.getVersjon(), endringPaAlleFelt(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enAvtaleMedAltUtfyltGodkjentAvVeileder() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        return avtale;
    }

    private static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("88888899999");
        BedriftNr bedriftNr = new BedriftNr("12345678");
        return new OpprettAvtale(deltakerFnr, bedriftNr);
    }

    public static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    public static EndreAvtale endringPaAlleFelt() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Fornavn");
        endreAvtale.setDeltakerEtternavn("Etternavn");
        endreAvtale.setDeltakerTlf("22334455");
        endreAvtale.setBedriftNavn("Bedriftnavn");
        endreAvtale.setBedriftNr(new BedriftNr("12345678"));
        endreAvtale.setArbeidsgiverFornavn("AG fornavn");
        endreAvtale.setArbeidsgiverEtternavn("AG etternavn");
        endreAvtale.setArbeidsgiverTlf("AG tlf");
        endreAvtale.setVeilederFornavn("Veilederfornavn");
        endreAvtale.setVeilederEtternavn("Veilederetternavn");
        endreAvtale.setVeilederTlf("Veiledertlf");
        endreAvtale.setOppfolging("Oppfolging");
        endreAvtale.setTilrettelegging("Tilrettelegging");
        endreAvtale.setStartDato(LocalDate.now());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(2));
        endreAvtale.setStillingprosent(50);
        endreAvtale.setMaal(List.of(TestData.etMaal(), TestData.etMaal()));
        endreAvtale.setOppgaver(List.of(TestData.enOppgave(), TestData.enOppgave()));
        return endreAvtale;
    }

    static Deltaker enDeltaker() {
        return new Deltaker(new Fnr("01234567890"), enAvtale());
    }

    public static Deltaker enDeltaker(Avtale avtale) {
        return new Deltaker(avtale.getDeltakerFnr(), avtale);
    }

    public static InnloggetSelvbetjeningBruker enSelvbetjeningBruker() {
        return new InnloggetSelvbetjeningBruker(new Fnr("99999999999"), emptyList());
    }

    public static InnloggetNavAnsatt enNavAnsatt() {
        return new InnloggetNavAnsatt(new NavIdent("F8888888"), mock(TilgangskontrollService.class));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        return new Arbeidsgiver(new Fnr("12345678901"), enAvtale());
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(TestData.etFodselsnummer(), avtale);
    }

    public static Fnr etFodselsnummer() {
        return new Fnr("00000000000");
    }

    public static Veileder enVeileder() {
        return new Veileder(new NavIdent("X123456"), enAvtale());
    }

    public static Veileder enVeileder(Avtale avtale) {
        return new Veileder(avtale.getVeilederNavIdent(), avtale);
    }

    public static Oppgave enOppgave() {
        return new Oppgave();
    }

    public static Maal etMaal() {
        return new Maal();
    }

    public static GodkjentPaVegneGrunn enGodkjentPaVegneGrunn() {
        GodkjentPaVegneGrunn paVegneGrunn = new GodkjentPaVegneGrunn();
        paVegneGrunn.setIkkeBankId(true);
        return paVegneGrunn;
    }

    public static InnloggetSelvbetjeningBruker innloggetSelvbetjeningBrukerMedOrganisasjon(Avtalepart<Fnr> avtalepartMedFnr) {
        Organisasjon organisasjon = new Organisasjon(avtalepartMedFnr.getAvtale().getBedriftNr(), avtalepartMedFnr.getAvtale().getBedriftNavn());
        return new InnloggetSelvbetjeningBruker(avtalepartMedFnr.getIdentifikator(), Arrays.asList(organisasjon));
    }

    public static InnloggetSelvbetjeningBruker innloggetSelvbetjeningBrukerUtenOrganisasjon(Avtalepart<Fnr> avtalepartMedFnr) {
        return new InnloggetSelvbetjeningBruker(avtalepartMedFnr.getIdentifikator(), emptyList());
    }

    public static InnloggetNavAnsatt innloggetNavAnsatt(Avtalepart<NavIdent> avtalepartMedNavIdent) {
        return new InnloggetNavAnsatt(avtalepartMedNavIdent.getIdentifikator(), mock(TilgangskontrollService.class));
    }

    public static Identifikator enIdentifikator() {
        return new Identifikator("test-id");
    }

    public static VarslbarHendelse enHendelse(Avtale avtale) {
        return VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.OPPRETTET);
    }

    public static BjelleVarsel etBjelleVarsel(Avtale avtale) {
        return BjelleVarsel.nyttVarsel(TestData.enIdentifikator(), TestData.enHendelse(avtale));
    }

    public static SmsVarsel etSmsVarsel(Avtale avtale) {
        return SmsVarsel.nyttVarsel("tlf", TestData.enIdentifikator(), "", null);
    }
}
