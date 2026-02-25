package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.ArbeidsgiverMutationRequest;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    prefix = "tiltaksgjennomforing.notifikasjoner",
    name = "enabled",
    havingValue = "false"
)
public class NotifikasjonServiceFake implements NotifikasjonService {

    @Override
    public String opprettNotifikasjon(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest) {
        return "";
    }

    @Override
    public String getAvtaleLenke(Avtale avtale) {
        return "";
    }

    @Override
    public void opprettNyBeskjed(
        ArbeidsgiverNotifikasjon notifikasjon,
        NotifikasjonMerkelapp merkelapp,
        NotifikasjonTekst tekst
    ) {}

    @Override
    public void opprettOppgave(
        ArbeidsgiverNotifikasjon notifikasjon,
        NotifikasjonMerkelapp merkelapp,
        NotifikasjonTekst tekst
    ) {}

    @Override
    public void oppgaveUtfoert(
        Avtale avtale,
        HendelseType hendelseTypeSomSkalMerkesUtfoert,
        MutationStatus status,
        HendelseType hendelseTypeForNyNotifikasjon
    ) {}

    @Override
    public void softDeleteNotifikasjoner(Avtale avtale) {}
}
