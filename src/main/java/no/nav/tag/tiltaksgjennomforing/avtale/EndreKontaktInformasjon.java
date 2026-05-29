package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

@Value
public class EndreKontaktInformasjon {
    String deltakerFornavn;
    String deltakerEtternavn;
    String deltakerTlf;
    String veilederFornavn;
    String veilederEtternavn;
    String veilederTlf;
    String arbeidsgiverFornavn;
    String arbeidsgiverEtternavn;
    String arbeidsgiverTlf;
    RefusjonKontaktperson refusjonKontaktperson;

    public boolean harMangler() {
        boolean harMangler =  Utils.erNoenTomme(
            deltakerFornavn,
            deltakerEtternavn,
            deltakerTlf,
            veilederFornavn,
            veilederEtternavn,
            veilederTlf,
            arbeidsgiverFornavn,
            arbeidsgiverEtternavn,
            arbeidsgiverTlf
        );
        if (refusjonKontaktperson == null || refusjonKontaktperson.erTom()) {
            return harMangler;
        }
        return harMangler || refusjonKontaktperson.harMangler();
    }
}
