package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

@Value
public class EndreOmMentor {
    String mentorFornavn;
    String mentorEtternavn;
    String mentorTlf;
    String mentorOppgaver;
    @Deprecated
    Double mentorAntallTimer;
    Double mentorAntallTimerPerMnd;
    Integer mentorTimelonn;
}
