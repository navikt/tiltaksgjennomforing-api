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


     public RefusjonKontaktperson() {
          this.refusjonKontaktpersonFornavn = "";
          this.refusjonKontaktpersonEtternavn = "";
          this.refusjonKontaktpersonTlf = "";
     }
}
