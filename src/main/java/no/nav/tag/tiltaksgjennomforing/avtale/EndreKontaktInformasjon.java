package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

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
    String bedriftNavn;
    RefusjonKontaktperson refusjonKontaktperson;
    VtaoFelter vtao;
}
