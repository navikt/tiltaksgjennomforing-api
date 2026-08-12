package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.InnsatsgruppeException;
import no.nav.tag.tiltaksgjennomforing.logging.TeamLogs;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        boolean erGyldig = Optional.ofNullable(oppfølgingStatus.getInnsatsgruppe())
            .map(innsatsgruppe -> innsatsgruppe.erGyldig(tiltakstype))
            .orElse(false);

        if (!erGyldig) {
            throw InnsatsgruppeException.fraTiltakstype(tiltakstype);
        }

        return oppfølgingStatus;
    }

    public Oppfølgingsstatus hentOppfolging(Avtale avtale) {
        return hentOppfolging(avtale.getDeltakerFnr());
    }

    public Oppfølgingsstatus hentOppfolging(Fnr fnr) {
        HentOppfolgingsstatusRespons oppfølgingsstatus = hentOppfølgingstatus(fnr);
        Gjeldende14aVedtakRespons innsatsgruppe = hentInnsatsgruppe(fnr);

        try {
            return new Oppfølgingsstatus(
                Formidlingsgruppe.parse(oppfølgingsstatus.formidlingsgruppe()),
                Kvalifiseringsgruppe.parse(oppfølgingsstatus.servicegruppe()),
                oppfølgingsstatus.oppfolgingsenhet().enhetId(),
                innsatsgruppe.innsatsgruppe()
            );
        } catch (Exception e) {
            log.error("Feil ved parsing av oppfølgingsstatus", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }
    }

    private Gjeldende14aVedtakRespons hentInnsatsgruppe(Fnr fnr) {
        Optional<Gjeldende14aVedtakRespons> responsOpt;

        try {
            responsOpt = veilarbvedtaksstotteClient.hentGjeldende14aVedtak(new Gjeldende14aVedtakRequest(fnr.asString()));
        } catch (Exception e) {
            log.error("Feil ved henting av gjeldende § 14 a-vedtak fra veilarbvedtaksstotte", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (responsOpt.isEmpty()) {
            teamLogs.info("Fant ikke gjeldende § 14 a-vedtak for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }

        return responsOpt.get();
    }

    private HentOppfolgingsstatusRespons hentOppfølgingstatus(Fnr fnr) {
        Optional<HentOppfolgingsstatusRespons> responsOpt;

        try {
            responsOpt = veilarboppfolgingClient.hentOppfolgingsstatus(new HentOppfolgingsstatusRequest(fnr.asString()));
        } catch (Exception e) {
            log.error("Feil ved henting av oppfølgingsstatus fra veilarboppfolging", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (responsOpt.isEmpty()) {
            teamLogs.info("Fant ikke oppfølgingsstatus for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }

        HentOppfolgingsstatusRespons respons = responsOpt.get();

        if (respons.oppfolgingsenhet() == null || respons.oppfolgingsenhet().enhetId() == null) {
            log.info("Fant ingen enhet. Deltaker er sannsynligvis ikke under oppfølging.");
            throw new FeilkodeException(Feilkode.ENHET_MANGLER);
        }

        teamLogs.info(
            "Hentet servicegruppe {} og formidlingsgruppe {} for fnr {}",
            respons.servicegruppe(),
            respons.formidlingsgruppe(),
            fnr
        );

        return respons;
    }
}
