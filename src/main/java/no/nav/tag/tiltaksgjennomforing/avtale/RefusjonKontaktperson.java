package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

@Value
@Embeddable
@AllArgsConstructor
@FieldNameConstants
public class RefusjonKontaktperson {
     String refusjonKontaktpersonFornavn;
     String refusjonKontaktpersonEtternavn;
     String refusjonKontaktpersonTlf;
     Boolean ønskerVarslingOmRefusjon;

     public RefusjonKontaktperson() {
          this.refusjonKontaktpersonFornavn = "";
          this.refusjonKontaktpersonEtternavn = "";
          this.refusjonKontaktpersonTlf = "";
          this.ønskerVarslingOmRefusjon = true;
     }

     public boolean erTom() {
          return refusjonKontaktpersonEtternavn == null &&
              refusjonKontaktpersonFornavn == null &&
              refusjonKontaktpersonTlf == null &&
              ønskerVarslingOmRefusjon == null;
     }

     public boolean harMangler() {
          return Utils.erNoenTomme(
              refusjonKontaktpersonFornavn,
              refusjonKontaktpersonEtternavn,
              refusjonKontaktpersonTlf
          );
     }
}
