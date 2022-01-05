package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import javax.persistence.Embeddable;

@Data
@Embeddable

public class RefusjonKontaktperson {

     String refusjonKontaktpersonFornavn;
     String refusjonKontaktpersonEtternavn;
     String refusjonKontaktpersonTlf;

     boolean ønskerInformasjonOmRefusjon;

}
