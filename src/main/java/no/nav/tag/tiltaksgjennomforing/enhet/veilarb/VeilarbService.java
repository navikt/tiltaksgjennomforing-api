package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.logging.TeamLogs;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class VeilarbService {
    private static final TeamLogs teamLogs = TeamLogs.getLogger(log);

    private final VeilarboppfolgingClient veilarboppfolgingClient;
    private final VeilarbvedtaksstotteClient veilarbvedtaksstotteClient;

    public VeilarbService(VeilarboppfolgingClient veilarboppfolgingClient, VeilarbvedtaksstotteClient veilarbvedtaksstotteClient) {
        this.veilarboppfolgingClient = veilarboppfolgingClient;
        this.veilarbvedtaksstotteClient = veilarbvedtaksstotteClient;
    }

    public Oppfølgingsstatus hentOgSjekkOppfolgingstatus(Avtale avtale) {
        return hentOgSjekkOppfolgingstatus(avtale.getDeltakerFnr(), avtale.getTiltakstype());
    }

    public Oppfølgingsstatus hentOgSjekkOppfolgingstatus(Fnr fnr, Tiltakstype tiltakstype) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfolging(fnr);

        if (tiltakstype.isSommerjobb()) {
            return oppfølgingStatus;
        }

        if (oppfølgingStatus.getKvalifiseringsgruppe().isUgyldigKvalifiseringsgruppe()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_IKKE_RETTIGHET);
        }

        if (tiltakstype.isMidlerTidiglonnstilskuddEllerSommerjobbEllerMentor() &&
            !oppfølgingStatus.getKvalifiseringsgruppe()
                .isKvalifisererTilMidlertidiglonnstilskuddOgSommerjobbOgMentor()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL);
        }

        if (tiltakstype.isVariglonnstilskudd() &&
            !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilVariglonnstilskudd()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_VARIG_LONNTILSKUDD_FEIL);
        }

        if (tiltakstype.isVTAO() &&
            !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilVTAO()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_VTAO_FEIL);
        }

        if (tiltakstype.isFirearigLonnstilskudd() &&
            !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilFirearigLonnstilskuddForUnge()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_FIREARIG_LONNTILSKUDD_FOR_UNGE_FEIL);
        }

        return oppfølgingStatus;
    }

    public Oppfølgingsstatus hentOppfolging(Fnr fnr) {
        return hentOppfolging(null, fnr);
    }

    public Oppfølgingsstatus hentOppfolging(Avtale avtale) {
        return hentOppfolging(avtale.getId(), avtale.getDeltakerFnr());
    }

    public Innsatsgruppe hentInnsatsgruppe(Fnr fnr) {
        Optional<Gjeldende14aVedtakRespons> respons;

        try {
            respons = veilarbvedtaksstotteClient.hentGjeldende14aVedtak(new Gjeldende14aVedtakRequest(fnr.asString()));
        } catch (Exception e) {
            log.warn("Feil ved henting av gjeldende § 14 a-vedtak fra veilarbvedtaksstotte", e);
            return null;
        }

        if (respons.isEmpty()) {
            teamLogs.info("Fant ikke gjeldende § 14 a-vedtak for fnr {}", fnr);
        }

        return respons.map(Gjeldende14aVedtakRespons::innsatsgruppe).orElse(null);
    }

    private Oppfølgingsstatus hentOppfolging(UUID avtaleId, Fnr fnr) {
        Oppfølgingsstatus oppfølgingsstatus = hentOppfølgingstatus(fnr);
        Innsatsgruppe innsatsgruppeObo = hentInnsatsgruppe(fnr);

        Kvalifiseringsgruppe kvalifiseringsgruppe = oppfølgingsstatus.getKvalifiseringsgruppe();
        Innsatsgruppe innsatsgruppeArena = kvalifiseringsgruppe.getInnsatsgruppe();

        if (!Innsatsgruppe.isArenaOboEqual(innsatsgruppeArena, innsatsgruppeObo)) {
            log.warn(
                "14a-diff{} kvalifiseringsgruppe(arena)={} (som tilsvarer {}), vedtak(obo)={}",
                avtaleId != null ? " for avtale=" + avtaleId + " -" : " -",
                kvalifiseringsgruppe,
                innsatsgruppeArena,
                innsatsgruppeObo
            );
        }

        return new Oppfølgingsstatus(
            oppfølgingsstatus.getFormidlingsgruppe(),
            oppfølgingsstatus.getKvalifiseringsgruppe(),
            oppfølgingsstatus.getOppfolgingsenhet(),
            innsatsgruppeObo
        );
    }

    private Oppfølgingsstatus hentOppfølgingstatus(Fnr fnr) {
        Optional<HentOppfolgingsstatusRespons> responsOpt;

        try {
            responsOpt = veilarboppfolgingClient.hentOppfolgingsstatus(new HentOppfolgingsstatusRequest(fnr.asString()));
        } catch (Exception e) {
            log.error("Feil ved henting av oppfølgingsstatus fra veilarboppfolging", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (responsOpt.isEmpty()) {
            teamLogs.info("Fant ikke innsatsbehov for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }

        HentOppfolgingsstatusRespons respons = responsOpt.get();
        teamLogs.info(
            "Hentet servicegruppe {} og formidlingsgruppe {} for fnr {}",
            respons.servicegruppe(),
            respons.formidlingsgruppe(),
            fnr
        );

        Optional<String> enhet = Optional.ofNullable(respons.oppfolgingsenhet()).
            map(HentOppfolgingsstatusRespons.Oppfolgingsenhet::enhetId);

        if (enhet.isEmpty()) {
            log.info("Fant ingen enhet. Deltaker er sannsynligvis ikke under oppfølging.");
        }

        try {
            return new Oppfølgingsstatus(
                Formidlingsgruppe.parse(respons.formidlingsgruppe()),
                Kvalifiseringsgruppe.parse(respons.servicegruppe()),
                enhet.orElse(null),
                null
            );
        } catch (Exception e) {
            log.error("Feil ved parsing av oppfølgingsstatus", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }
    }
}
