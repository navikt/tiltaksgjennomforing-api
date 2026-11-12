package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
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
        Gjeldende14aVedtakRespons vedtak = hentInnsatsgruppe(fnr);
        Oppfolgingsenhet oppfolgingsenhet = hentOppfolgingsenhet(fnr);

        return new Oppfølgingsstatus(oppfolgingsenhet.id(), vedtak.innsatsgruppe());
    }

    private Gjeldende14aVedtakRespons hentInnsatsgruppe(Fnr fnr) {
        Optional<Gjeldende14aVedtakRespons> responsOpt;

        try {
            responsOpt = veilarbvedtaksstotteClient.hentGjeldende14aVedtak(new Gjeldende14aVedtakRequest(fnr.asString()));
        } catch (Exception e) {
            log.error("Feil ved henting av gjeldende § 14 a-vedtak fra veilarbvedtaksstotte", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (responsOpt.isEmpty() || responsOpt.get().innsatsgruppe() == null) {
            teamLogs.info("Fant ikke gjeldende § 14 a-vedtak for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }

        return responsOpt.get();
    }

    private Oppfolgingsenhet hentOppfolgingsenhet(Fnr fnr) {
        Optional<Oppfolgingsenhet> enhetOpt;

        try {
            enhetOpt = veilarboppfolgingClient.hentOppfolgingsenhet(new HentOppfolgingsenhetRequest(fnr.asString()));
        } catch (Exception e) {
            log.error("Feil ved henting av oppfølgingsenhet fra veilarboppfolging", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (enhetOpt.isEmpty() || enhetOpt.get().id() == null) {
            log.info("Fant ingen oppfølgingsenhet. Deltaker er sannsynligvis ikke under oppfølging.");
            teamLogs.info("Fant ingen oppfølgingsenhet for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.ENHET_MANGLER);
        }

        Oppfolgingsenhet enhet = enhetOpt.get();

        if (!enhet.erFraArena()) {
            log.info("Oppfølgingsenhet har kilde {}, ikke ARENA. Deltaker er sannsynligvis ikke under oppfølging.", enhet.kilde());
            throw new FeilkodeException(Feilkode.ENHET_MANGLER);
        }

        return enhet;
    }
}
