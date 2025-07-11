package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.logging.TeamLogs;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class VeilarboppfolgingService {

    private final TeamLogs teamLogs = TeamLogs.getLogger(log);

    private final VeilarboppfolgingClient client;

    public VeilarboppfolgingService(VeilarboppfolgingClient client) {
        this.client = client;
    }

    public Oppfølgingsstatus hentOppfolgingsstatus(Fnr fnr) {
        return hentOppfolgingsstatus(fnr.asString());
    }

    public Oppfølgingsstatus hentOppfolgingsstatus(String fnr) {
        Optional<HentOppfolgingsstatusRespons> responsOpt;

        try {
            responsOpt = client.hentOppfolgingsstatus(new HentOppfolgingsstatusRequest(fnr));
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

        try {
            return new Oppfølgingsstatus(
                Formidlingsgruppe.parse(respons.formidlingsgruppe()),
                Kvalifiseringsgruppe.parse(respons.servicegruppe()),
                respons.oppfolgingsenhet().enhetId()
            );
        } catch (Exception e) {
            log.error("Feil ved parsing av oppfølgingsstatus", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }
    }

    public Oppfølgingsstatus hentOgSjekkOppfolgingstatus(Avtale avtale) {
        return hentOgSjekkOppfolgingstatus(avtale.getDeltakerFnr(), avtale.getTiltakstype());
    }

    public Oppfølgingsstatus hentOgSjekkOppfolgingstatus(Fnr fnr, Tiltakstype tiltakstype) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfolgingsstatus(fnr);
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

        return oppfølgingStatus;
    }

}
