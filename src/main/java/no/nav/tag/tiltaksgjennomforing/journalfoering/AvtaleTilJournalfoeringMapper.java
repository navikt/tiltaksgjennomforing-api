package no.nav.tag.tiltaksgjennomforing.journalfoering;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.AvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.AvtaleInnholdDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.MaalDTO;
import no.nav.tag.tiltaksgjennomforing.utils.DatoUtils;

import java.util.ArrayList;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;

public class AvtaleTilJournalfoeringMapper {

    public static AvtaleTilJournalfoering tilJournalfoering(AvtaleInnholdDTO avtaleInnhold, AvtaleDTO avtale, Avtalerolle avtalerolle) {
        //AvtaleInnholdDTO avtaleInnhold = avtale.gjeldendeInnhold();

        AvtaleTilJournalfoering avtaleTilJournalfoering = new AvtaleTilJournalfoering();
        avtaleTilJournalfoering.setTiltakstype(avtale.tiltakstype());
        avtaleTilJournalfoering.setArbeidsgiverKontonummer(avtale.gjeldendeInnhold().arbeidsgiverKontonummer());
        avtaleTilJournalfoering.setStillingstittel(avtale.gjeldendeInnhold().stillingstittel());
        avtaleTilJournalfoering.setArbeidsoppgaver(avtale.gjeldendeInnhold().arbeidsoppgaver());
        avtaleTilJournalfoering.setLonnstilskuddProsent(avtale.gjeldendeInnhold().lonnstilskuddProsent());
        avtaleTilJournalfoering.setManedslonn(avtale.gjeldendeInnhold().manedslonn());
        avtaleTilJournalfoering.setFeriepengesats(avtale.gjeldendeInnhold().feriepengesats());
        avtaleTilJournalfoering.setArbeidsgiveravgift(avtale.gjeldendeInnhold().arbeidsgiveravgift());
        avtaleTilJournalfoering.setMentorFornavn(avtale.gjeldendeInnhold().mentorFornavn());
        avtaleTilJournalfoering.setMentorEtternavn(avtale.gjeldendeInnhold().mentorEtternavn());
        avtaleTilJournalfoering.setMentorOppgaver(avtale.gjeldendeInnhold().mentorOppgaver());
        avtaleTilJournalfoering.setMentorAntallTimer(avtale.gjeldendeInnhold().mentorAntallTimer());
        avtaleTilJournalfoering.setMentorTimelonn(avtale.gjeldendeInnhold().mentorTimelonn());
        avtaleTilJournalfoering.setMentorTlf(avtale.gjeldendeInnhold().mentorTlf());
        avtaleTilJournalfoering.setMentorValgtLonnstype(mentorValgtLonnstypeTekst(avtaleInnhold.mentorValgtLonnstype()));
        avtaleTilJournalfoering.setMentorValgtLonnstypeBelop(avtaleInnhold.mentorValgtLonnstypeBelop());
        avtaleTilJournalfoering.setGodkjentAvArbeidsgiver(DatoUtils.instantTilLocalDate(avtaleInnhold.godkjentAvArbeidsgiver()));
        avtaleTilJournalfoering.setGodkjentAvVeileder(DatoUtils.instantTilLocalDate(avtaleInnhold.godkjentAvVeileder()));
        avtaleTilJournalfoering.setGodkjentAvDeltaker(DatoUtils.instantTilLocalDate(avtaleInnhold.godkjentAvDeltaker()));
        avtaleTilJournalfoering.setOpprettet(DatoUtils.instantTilLocalDate(avtale.opprettetTidspunkt()));
        avtaleTilJournalfoering.setAvtaleId(avtale.id());
        avtaleTilJournalfoering.setAvtaleVersjonId(avtaleInnhold.id());
        avtaleTilJournalfoering.setDeltakerFnr(identifikatorAsString(avtale.deltakerFnr()));
        avtaleTilJournalfoering.setMentorFnr(identifikatorAsString(avtale.mentorFnr()));
        avtaleTilJournalfoering.setBedriftNr(identifikatorAsString(avtale.bedriftNr()));
        avtaleTilJournalfoering.setVeilederNavIdent(identifikatorAsString(avtale.veilederNavIdent()));
        avtaleTilJournalfoering.setEnhetOppfolging(avtale.enhetOppfolging());
        avtaleTilJournalfoering.setDeltakerFornavn(avtaleInnhold.deltakerFornavn());
        avtaleTilJournalfoering.setDeltakerEtternavn(avtaleInnhold.deltakerEtternavn());
        avtaleTilJournalfoering.setDeltakerTlf(avtaleInnhold.deltakerTlf());
        avtaleTilJournalfoering.setBedriftNavn(avtaleInnhold.bedriftNavn());
        avtaleTilJournalfoering.setArbeidsgiverFornavn(avtaleInnhold.arbeidsgiverFornavn());
        avtaleTilJournalfoering.setArbeidsgiverEtternavn(avtaleInnhold.arbeidsgiverEtternavn());
        avtaleTilJournalfoering.setArbeidsgiverTlf(avtaleInnhold.arbeidsgiverTlf());
        avtaleTilJournalfoering.setVeilederFornavn(avtaleInnhold.veilederFornavn());
        avtaleTilJournalfoering.setVeilederEtternavn(avtaleInnhold.veilederEtternavn());
        avtaleTilJournalfoering.setVeilederTlf(avtaleInnhold.veilederTlf());
        avtaleTilJournalfoering.setOppfolging(avtaleInnhold.oppfolging());
        avtaleTilJournalfoering.setTilrettelegging(avtaleInnhold.tilrettelegging());
        avtaleTilJournalfoering.setStartDato(avtaleInnhold.startDato());
        avtaleTilJournalfoering.setOpprettetAar(DatoUtils.instantTilZonedDateTime(avtale.opprettetTidspunkt()).getYear());
        avtaleTilJournalfoering.setSluttDato(avtaleInnhold.sluttDato());
        avtaleTilJournalfoering.setStillingprosent(avtale.gjeldendeInnhold().stillingprosent());
        avtaleTilJournalfoering.setAntallDagerPerUke(avtale.gjeldendeInnhold().antallDagerPerUke());
        avtaleTilJournalfoering.setMaal(maalListToMaalTilJournalfoeringList(avtaleInnhold.maal()));
        avtaleTilJournalfoering.setGodkjentPaVegneGrunn(godkjentPaVegneGrunn(avtaleInnhold.godkjentPaVegneGrunn()));
        avtaleTilJournalfoering.setGodkjentPaVegneAv(avtaleInnhold.godkjentPaVegneAv());
        avtaleTilJournalfoering.setVersjon(avtaleInnhold.versjon());
        avtaleTilJournalfoering.setHarFamilietilknytning(avtaleInnhold.harFamilietilknytning());
        avtaleTilJournalfoering.setFamilietilknytningForklaring((avtaleInnhold.familietilknytningForklaring()));
        avtaleTilJournalfoering.setFeriepengerBelop(avtaleInnhold.feriepengerBelop());
        avtaleTilJournalfoering.setOtpSats(avtaleInnhold.otpSats());
        avtaleTilJournalfoering.setOtpBelop(avtaleInnhold.otpBelop());
        avtaleTilJournalfoering.setArbeidsgiveravgiftBelop(avtaleInnhold.arbeidsgiveravgiftBelop());
        avtaleTilJournalfoering.setSumLonnsutgifter(avtaleInnhold.sumLonnsutgifter());
        avtaleTilJournalfoering.setSumLonnstilskudd(avtaleInnhold.sumLonnstilskudd());
        avtaleTilJournalfoering.setStillingstype(avtaleInnhold.stillingstype());
        avtaleTilJournalfoering.setManedslonn100pst(avtaleInnhold.manedslonn100pst());
        avtaleTilJournalfoering.setRefusjonKontaktperson(avtaleInnhold.refusjonKontaktperson());
        avtaleTilJournalfoering.setInkluderingstilskuddsutgift(avtaleInnhold.inkluderingstilskuddsutgift());
        avtaleTilJournalfoering.setInkluderingstilskuddBegrunnelse(avtaleInnhold.inkluderingstilskuddBegrunnelse());

        if (avtalerolle != null) {
            avtaleTilJournalfoering.setAvtalerolle(avtalerolle);
        }
        if (avtaleInnhold.godkjentTaushetserklæringAvMentor() != null) {
            avtaleTilJournalfoering.setGodkjentTaushetserklæringAvMentor(DatoUtils.instantTilLocalDate(avtaleInnhold.godkjentTaushetserklæringAvMentor()));
        }

        if (avtale.tiltakstype().equals(Tiltakstype.VTAO)) {
            avtaleTilJournalfoering.setSumLonnstilskudd(VTAO_SATS.hentGjeldendeSats(avtale.opprettetTidspunkt()));
        }

        return avtaleTilJournalfoering;
    }

    private static String identifikatorAsString(Identifikator id) {
        return id != null ? id.asString() : "";
    }

    private static GodkjentPaVegneGrunnTilJournalfoering godkjentPaVegneGrunn(GodkjentPaVegneGrunn grunn) {
        if (grunn == null) {
            return null;
        }

        return new GodkjentPaVegneGrunnTilJournalfoering(
                grunn.isIkkeBankId(),
                grunn.isDigitalKompetanse(),
                grunn.isReservert(),
                grunn.isArenaMigreringDeltaker()
        );
    }


    private static MaalTilJournalfoering maalToMaalTilJournalfoering(MaalDTO maal) {
        if (maal == null) {
            return null;
        }

        MaalTilJournalfoering maalTilJournalfoering = new MaalTilJournalfoering();

        maalTilJournalfoering.setKategori(maal.kategori().getVerdi());
        maalTilJournalfoering.setBeskrivelse(maal.beskrivelse());

        return maalTilJournalfoering;
    }

    private static List<MaalTilJournalfoering> maalListToMaalTilJournalfoeringList(List<MaalDTO> list) {
        if (list == null) {
            return null;
        }
        List<MaalTilJournalfoering> list1 = new ArrayList<>(list.size());
        for (MaalDTO maal : list) {
            list1.add(maalToMaalTilJournalfoering(maal));
        }
        return list1;
    }

    private static String mentorValgtLonnstypeTekst(MentorValgtLonnstype mentorValgtLonnstype) {
        if (mentorValgtLonnstype == null) {
            return null;
        }
        return mentorValgtLonnstype.toString().substring(0, 1).toUpperCase() + mentorValgtLonnstype.toString().substring(1).toLowerCase();
    }
}
