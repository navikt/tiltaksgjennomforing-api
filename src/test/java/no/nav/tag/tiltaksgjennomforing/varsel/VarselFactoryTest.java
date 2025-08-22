package no.nav.tag.tiltaksgjennomforing.varsel;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class VarselFactoryTest {

  @BeforeEach
  public void setup() {
    FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
  }

  @AfterEach
  public void tearDown() {
    FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
  }

  @Test
  public void skal_returnere_tilskuddsperiode_verdi_i_teksten_naar_beslutter_godkjenner_periode(){
    Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder();
    TilskuddPeriode tilskuddPeriode = avtale.getGjeldendeTilskuddsperiode();
      VarselFactory factory = new VarselFactory(avtale, tilskuddPeriode, AvtaleHendelseUtførtAv.beslutter(TestData.enNavIdent()), HendelseType.TILSKUDDSPERIODE_GODKJENT);
    DateTimeFormatter norskDatoformat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    assertEquals("Tilskuddsperiode har blitt godkjent av beslutter\n(" + avtale.getGjeldendeTilskuddsperiode().getStartDato().format(norskDatoformat) + " til " + avtale.getGjeldendeTilskuddsperiode().getSluttDato().format(norskDatoformat) + ")",factory.veileder().getTekst());
  }

  @Test
  public void skal_returnere_tilskuddsperiode_uten_periode_i_tekstenom_om_start_og_slutt_periode_ikke_er_satt(){
    Avtale avtale = Mockito.mock(Avtale.class);
    TilskuddPeriode tilskuddPeriode = Mockito.mock(TilskuddPeriode.class);
    when(tilskuddPeriode.getStartDato()).thenReturn(null);
    when(tilskuddPeriode.getSluttDato()).thenReturn(null);
    when(avtale.getGjeldendeTilskuddsperiode()).thenReturn(tilskuddPeriode);
    VarselFactory factory = new VarselFactory(avtale, AvtaleHendelseUtførtAv.beslutter(TestData.enNavIdent()), HendelseType.TILSKUDDSPERIODE_GODKJENT);
    assertEquals("Tilskuddsperiode har blitt godkjent av beslutter",factory.veileder().getTekst());
  }
  @Test
  public void skal_returnere_tilskuddsperiode_er_null(){
    Avtale avtale = Mockito.mock(Avtale.class);
    when(avtale.getGjeldendeTilskuddsperiode()).thenReturn(null);
    VarselFactory factory = new VarselFactory(avtale, AvtaleHendelseUtførtAv.beslutter(TestData.enNavIdent()), HendelseType.TILSKUDDSPERIODE_GODKJENT);
    assertEquals("Tilskuddsperiode har blitt godkjent av beslutter",factory.veileder().getTekst());
  }
  @Test
  public void skal_returnere_4_parter_Mentor_Deltaker_Arbeidsgiver_Veileder_Ventor_I_VarselListe(){
    VarselFactory factory = new VarselFactory(TestData.enMentorAvtaleUsignert(), AvtaleHendelseUtførtAv.mentor(TestData.enNavIdent()), HendelseType.OPPRETTET);
    assertEquals(4,factory.alleParter().toArray().length);
  }

  @Test
  public void skal_returnere_3_parter_Deltaker_Arbeidsgiver_Veileder_Ventor_I_VarselListe(){
    VarselFactory factory = new VarselFactory(TestData.enArbeidstreningAvtale(), AvtaleHendelseUtførtAv.arbeidsgiver(TestData.enArbeidstreningAvtale()), HendelseType.OPPRETTET);
    assertEquals(3,factory.alleParter().toArray().length);
  }

}
