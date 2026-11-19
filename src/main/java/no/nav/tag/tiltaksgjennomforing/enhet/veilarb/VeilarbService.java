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

    private final AoOppfolgingskontorClient aoOppfolgingskontorClient;
    private final VeilarbvedtaksstotteClient veilarbvedtaksstotteClient;

    public VeilarbService(AoOppfolgingskontorClient aoOppfolgingskontorClient, VeilarbvedtaksstotteClient veilarbvedtaksstotteClient) {
        this.aoOppfolgingskontorClient = aoOppfolgingskontorClient;
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
        Oppfolgingskontor oppfolgingskontor = hentOppfolgingskontor(fnr);

        return new Oppfølgingsstatus(oppfolgingskontor.kontorId(), oppfolgingskontor.kontorNavn(), vedtak.innsatsgruppe());
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

    private Oppfolgingskontor hentOppfolgingskontor(Fnr fnr) {
        Optional<Oppfolgingskontor> kontorOpt;

        try {
            kontorOpt = aoOppfolgingskontorClient.hentOppfolgingskontor(new HentOppfolgingskontorRequest(fnr.asString()));
        } catch (Exception e) {
            log.error("Feil ved henting av oppfølgingskontor fra ao-oppfolgingskontor", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (kontorOpt.isEmpty() || kontorOpt.get().kontorId() == null) {
            log.info("Fant ikke oppfølgingskontor. Deltaker er sannsynligvis ikke under oppfølging.");
            teamLogs.info("Fant ikke oppfølgingskontor for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.ENHET_MANGLER);
        }

        Oppfolgingskontor kontor = kontorOpt.get();

        if (!kontor.erGyldigOppfolgingskontor()) {
            log.info("Oppfølgingskontor har kontortype {}. Deltaker er sannsynligvis ikke under oppfølging.", kontor.kontorType());
            throw new FeilkodeException(Feilkode.ENHET_MANGLER);
        }

        return kontor;
    }
}
