package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class FnrConverterTest {

  @Test
  public void skalReturnereNullOmVerdiErNull(){
    assertNull(new FnrConverter().convertToDatabaseColumn(null));
  }

}
