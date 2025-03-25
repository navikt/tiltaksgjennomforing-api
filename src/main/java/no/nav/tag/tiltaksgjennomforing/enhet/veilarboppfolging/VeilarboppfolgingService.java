package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte.Gjeldende14aVedtakRequest;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte.Gjeldende14aVedtakResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte.VeilarbvedtaksstotteClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.logging.SecureLog;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class VeilarboppfolgingService {
    private static final SecureLog secureLog = SecureLog.getLogger(log);

    private final VeilarboppfolgingClient client;
    private final VeilarbvedtaksstotteClient veilarbvedtaksstotteClient;

    public VeilarboppfolgingService(VeilarboppfolgingClient client, VeilarbvedtaksstotteClient veilarbvedtaksstotteClient) {
        this.client = client;
        this.veilarbvedtaksstotteClient = veilarbvedtaksstotteClient;

    }

    public Oppfølgingsstatus hentOppfolgingsstatus(String fnr) {
        Optional<HentOppfolgingsstatusRespons> responsOppfolginsstatusOpt;
        Optional<Gjeldende14aVedtakResponse> responseGjeldende14aVedtakOpt;

        try {
            responsOppfolginsstatusOpt = client.hentOppfolgingsstatus(new HentOppfolgingsstatusRequest(fnr));
            responseGjeldende14aVedtakOpt = veilarbvedtaksstotteClient.hentGjeldende14aVedtak(new Gjeldende14aVedtakRequest(fnr));
        } catch (Exception e) {
            log.error("Feil ved henting av oppfølgingsstatus fra veilarboppfolging eller veilarbvedtaksstotte", e);
            secureLog.error("Feil ved henting av oppfølgingsstatus for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }

        if (responsOppfolginsstatusOpt.isEmpty()) {
            secureLog.error("Fant ikke innsatsbehov for fnr {}", fnr);
            log.error("Fant ikke innsatsbehov (veilarbOppfølging)"); // Trigge alerterator
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }
        if (responseGjeldende14aVedtakOpt.isEmpty()) {
            secureLog.error("Fant ikke 14-a vedtak for fnr {}", fnr);
            log.error("Fant ikke innsatsbehov (veilarbVedtaksstøtte)"); // Trigge alerterator
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }

        HentOppfolgingsstatusRespons responsOppfolgingstatus = responsOppfolginsstatusOpt.get();
        Gjeldende14aVedtakResponse gjeldende14aVedtakResponse = responseGjeldende14aVedtakOpt.get();
        secureLog.info("Hentet servicegruppe {} og formidlingsgruppe {} for fnr {}", responsOppfolgingstatus.servicegruppe(), responsOppfolgingstatus.formidlingsgruppe(), fnr);
        secureLog.info("Hentet gjeldende 14-a vedtak {} for fnr {}", gjeldende14aVedtakResponse, fnr);
        sammenLignInnsatsgrupper(gjeldende14aVedtakResponse, responsOppfolgingstatus, fnr);

        try {
            return new Oppfølgingsstatus(
                    Formidlingsgruppe.parse(responsOppfolgingstatus.formidlingsgruppe()),
                    Kvalifiseringsgruppe.parse(responsOppfolgingstatus.servicegruppe()),
                    responsOppfolgingstatus.oppfolgingsenhet().enhetId()
            );
        } catch (Exception e) {
            log.error("Feil ved parsing av oppfølgingsstatus", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET);
        }
    }

    public Oppfølgingsstatus hentOgSjekkOppfolgingstatus(Avtale avtale) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfolgingsstatus(avtale.getDeltakerFnr().asString());
        if (avtale.getTiltakstype().isSommerjobb()) {
            return oppfølgingStatus;
        }

        if (oppfølgingStatus.getKvalifiseringsgruppe().isUgyldigKvalifiseringsgruppe()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_IKKE_RETTIGHET);
        }

        if (avtale.getTiltakstype().isMidlerTidiglonnstilskuddEllerSommerjobbEllerMentor() &&
                !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilMidlertidiglonnstilskuddOgSommerjobbOgMentor()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL);
        }

        if (avtale.getTiltakstype().isVariglonnstilskudd() &&
                !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilVariglonnstilskudd()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_VARIG_LONNTILSKUDD_FEIL);
        }

        if (avtale.getTiltakstype().isVTAO() &&
                !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilVTAO()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_VTAO_FEIL);
        }

        return oppfølgingStatus;
    }

    private void sammenLignInnsatsgrupper(Gjeldende14aVedtakResponse gjeldende14aVedtakResponse, HentOppfolgingsstatusRespons responsOppfolgingstatus, String fnr) {
        if (gjeldende14aVedtakResponse.innsatsgruppe() == null && responsOppfolgingstatus.servicegruppe() != null) {
            log.warn("Fant ikke innsatsgruppe i 14a vedtak, men fikk en servicegruppe fra veilarboppfolging: {}", responsOppfolgingstatus.servicegruppe());
            secureLog.warn("Fant ikke innsatsgruppe i 14a vedtak, men fikk en servicegruppe fra veilarboppfolging: {} for fnr: {}", responsOppfolgingstatus.servicegruppe(), fnr);
            return;
        }
        if (!gjeldende14aVedtakResponse.innsatsgruppe().getArenaKode().equals(responsOppfolgingstatus.servicegruppe())) {
            log.warn("innsatsgruppe fra veilarbvedtaksstotte ({}) matcher ikke servicegruppe fra veilarboppfolging ({})",
                    gjeldende14aVedtakResponse.innsatsgruppe().getArenaKode(), responsOppfolgingstatus.servicegruppe());
            secureLog.warn("innsatsgruppe fra veilarbvedtaksstotte ({}) matcher ikke servicegruppe fra veilarboppfolging ({}) for fnr: {}",
                    gjeldende14aVedtakResponse.innsatsgruppe().getArenaKode(), responsOppfolgingstatus.servicegruppe(), fnr);
        }
    }

}
