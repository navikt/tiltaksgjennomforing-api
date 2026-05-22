package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

@Value
public class EndreOmMentor {
    String mentorFornavn;
    String mentorEtternavn;
    String mentorTlf;
    String mentorOppgaver;
    Double mentorAntallTimer;
    Integer mentorTimelonn;

    public boolean harMangler() {
        return Utils.erNoenTomme(
            mentorFornavn,
            mentorEtternavn,
            mentorTlf,
            mentorTimelonn,
            mentorAntallTimer,
            mentorOppgaver
        );
    }
}
