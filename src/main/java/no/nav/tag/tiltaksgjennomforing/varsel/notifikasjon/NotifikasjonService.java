package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.ArbeidsgiverMutationRequest;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;

public interface NotifikasjonService {
    String opprettNotifikasjon(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest);

    String getAvtaleLenke(Avtale avtale);

    void opprettNyBeskjed(
        ArbeidsgiverNotifikasjon notifikasjon,
        NotifikasjonMerkelapp merkelapp,
        NotifikasjonTekst tekst
    );

    void opprettOppgave(
        ArbeidsgiverNotifikasjon notifikasjon,
        NotifikasjonMerkelapp merkelapp,
        NotifikasjonTekst tekst
    );

    void oppgaveUtfoert(
        Avtale avtale,
        HendelseType hendelseTypeSomSkalMerkesUtfoert,
        MutationStatus status,
        HendelseType hendelseTypeForNyNotifikasjon
    );

    void softDeleteNotifikasjoner(Avtale avtale);
}
