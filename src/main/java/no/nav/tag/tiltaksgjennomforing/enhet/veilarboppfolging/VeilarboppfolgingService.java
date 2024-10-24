package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VeilarboppfolgingService {

    private final VeilarboppfolgingClient client;

    public VeilarboppfolgingService(VeilarboppfolgingClient client) {
        this.client = client;
    }

    public Oppfølgingsstatus hentOppfolgingsstatus(String fnr) {
        try {
            HentOppfolgingsstatusRespons respons = client.hentOppfolgingsstatus(new HentOppfolgingsstatusRequest(fnr));

            return new Oppfølgingsstatus(
                Formidlingsgruppe.parse(respons.formidlingsgruppe()),
                Kvalifiseringsgruppe.parse(respons.servicegruppe()),
                respons.oppfolgingsenhet().enhetId()
            );
        } catch (Exception e) {
            log.error("Feil ved henting av oppfølgingsstatus fra veilarboppfolging", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATS_BEHOV_FEILET);
        }
    }

    private boolean erMidlerTidiglonnstilskuddEllerSommerjobbEllerMentor(Tiltakstype tiltakstype) {
        return (tiltakstype == Tiltakstype.SOMMERJOBB ||
                tiltakstype == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD) ||
                tiltakstype == Tiltakstype.MENTOR;
    }

    private boolean erVariglonnstilskudd(Tiltakstype tiltakstype) {
        return tiltakstype.equals(Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    public Oppfølgingsstatus sjekkOgHentOppfølgingStatus(Avtale avtale) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfolgingsstatus(avtale.getDeltakerFnr().asString());
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            sjekkStatus(avtale, oppfølgingStatus);
        }
        return oppfølgingStatus;
    }

    public void sjekkOppfølgingStatus(Avtale avtale) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfolgingsstatus(avtale.getDeltakerFnr().asString());
        sjekkStatus(avtale, oppfølgingStatus);
    }

    private void sjekkStatus(Avtale avtale, Oppfølgingsstatus oppfølgingStatus) {
        if (
                oppfølgingStatus == null ||
                        oppfølgingStatus.getFormidlingsgruppe() == null ||
                        oppfølgingStatus.getKvalifiseringsgruppe() == null
        ) {
            throw new FeilkodeException(Feilkode.HENTING_AV_INNSATS_BEHOV_FEILET);
        }

        if (oppfølgingStatus.getKvalifiseringsgruppe().isUgyldigKvalifiseringsgruppe()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_IKKE_RETTIGHET);
        }

        if (erMidlerTidiglonnstilskuddEllerSommerjobbEllerMentor(avtale.getTiltakstype()) &&
            !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilMidlertidiglonnstilskuddOgSommerjobbOgMentor()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL);
        }

        if (erVariglonnstilskudd(avtale.getTiltakstype()) &&
            !oppfølgingStatus.getKvalifiseringsgruppe().isKvalifisererTilVariglonnstilskudd()) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_VARIG_LONNTILSKUDD_FEIL);
        }
    }

}
