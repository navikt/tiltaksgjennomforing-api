package no.nav.tag.tiltaksgjennomforing.varsel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VarselFactoryTest {

  @Test
  public void skal_returnere_tilskuddsperiode_verdi_i_teksten_naar_beslutter_godkjenner_periode(){
    Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder();
    VarselFactory factory = new VarselFactory(avtale, Avtalerolle.BESLUTTER, TestData.enNavIdent() , HendelseType.TILSKUDDSPERIODE_GODKJENT);
    assertEquals("Tilskuddsperiode har blitt godkjent av beslutter (" + avtale.gjeldendeTilskuddsperiode().getStartDato() + " til " + avtale.gjeldendeTilskuddsperiode().getSluttDato() + ")",factory.veileder().getTekst());
  }

  @Test
  public void skal_returnere_tilskuddsperiode_uten_periode_i_tekstenom_om_start_og_slutt_periode_ikke_er_satt(){
    Avtale avtale = Mockito.mock(Avtale.class);
    TilskuddPeriode tilskuddPeriode = Mockito.mock(TilskuddPeriode.class);
    when(tilskuddPeriode.getStartDato()).thenReturn(null);
    when(tilskuddPeriode.getSluttDato()).thenReturn(null);
    when(avtale.gjeldendeTilskuddsperiode()).thenReturn(tilskuddPeriode);
    VarselFactory factory = new VarselFactory(avtale, Avtalerolle.BESLUTTER, TestData.enNavIdent() , HendelseType.TILSKUDDSPERIODE_GODKJENT);
    assertEquals("Tilskuddsperiode har blitt godkjent av beslutter",factory.veileder().getTekst());
  }
  @Test
  public void skal_returnere_4_parter_Mentor_Deltaker_Arbeidsgiver_Veileder_Ventor_I_VarselListe(){
    VarselFactory factory = new VarselFactory(TestData.enMentorAvtaleUsignert(), Avtalerolle.MENTOR, TestData.enNavIdent() , HendelseType.OPPRETTET);
    assertEquals(4,factory.alleParter().toArray().length);
  }

  @Test
  public void skal_returnere_3_parter_Deltaker_Arbeidsgiver_Veileder_Ventor_I_VarselListe(){
    VarselFactory factory = new VarselFactory(TestData.enArbeidstreningAvtale(), Avtalerolle.ARBEIDSGIVER, TestData.enNavIdent(), HendelseType.OPPRETTET);
    assertEquals(3,factory.alleParter().toArray().length);
  }

}