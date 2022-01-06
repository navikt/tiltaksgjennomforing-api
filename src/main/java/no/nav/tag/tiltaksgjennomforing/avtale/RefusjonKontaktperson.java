package no.nav.tag.tiltaksgjennomforing.avtale;

import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@Embeddable
@AllArgsConstructor
public class RefusjonKontaktperson {

     String refusjonKontaktpersonFornavn;
     String refusjonKontaktpersonEtternavn;
     String refusjonKontaktpersonTlf;
     boolean ønskerInformasjonOmRefusjon;

     public RefusjonKontaktperson() {
          this.refusjonKontaktpersonFornavn = "";
          this.refusjonKontaktpersonEtternavn = "";
          this.refusjonKontaktpersonTlf = "";
          this.ønskerInformasjonOmRefusjon = true;
     }
}
