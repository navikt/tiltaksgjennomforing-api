package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
public class VeilarboppfolgingService {

    private final VeilarboppfolgingClient client;

    public VeilarboppfolgingService(VeilarboppfolgingClient client) {
        this.client = client;
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
            log.info(
                "Fant ikke innsatsbehov for id {}",
                Hashing.sha256().hashString(fnr + VeilarboppfolgingService.class.getName(), StandardCharsets.UTF_8)
            );
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }

        HentOppfolgingsstatusRespons respons = responsOpt.get();
        log.info("Hentet servicegruppe {} og formidlingsgruppe {} for id {}",
            respons.servicegruppe(),
            respons.formidlingsgruppe(),
            Hashing.sha256().hashString(fnr + VeilarboppfolgingService.class.getName(), StandardCharsets.UTF_8)
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

        return oppfølgingStatus;
    }

}
