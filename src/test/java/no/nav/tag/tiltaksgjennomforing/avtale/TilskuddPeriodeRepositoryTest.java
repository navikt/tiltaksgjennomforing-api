package no.nav.tag.tiltaksgjennomforing.avtale;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(Miljø.LOCAL)
@DirtiesContext
class TilskuddPeriodeRepositoryTest {

  @Autowired
  private TilskuddPeriodeRepository tilskuddPeriodeRepository;
  @Autowired
  private AvtaleRepository avtaleRepository;

  private TilskuddPeriode tilskuddPeriode;
  private Avtale lagretAvtale;

  @BeforeEach
  public void setup() {
    tilskuddPeriodeRepository.deleteAll();
    tilskuddPeriode = new TilskuddPeriode();
    lagretAvtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    avtaleRepository.save(lagretAvtale);
  }

  @Test
  public void skalKunne_hente_avale_med_UBEHANDLET_tilskuddPeriode() {
    // Lage avtale
    tilskuddPeriode.setAvtaleInnhold(lagretAvtale.gjeldendeInnhold());
    lagretAvtale.gjeldendeInnhold().setTilskuddPeriode(Collections.singletonList(tilskuddPeriode));
    tilskuddPeriode.setBeløp(10);
    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
    tilskuddPeriodeRepository.save(tilskuddPeriode);

    List<TilskuddPeriode> tilskuddPeriodes = tilskuddPeriodeRepository.findAllByStatus(TilskuddPeriodeStatus.UBEHANDLET);
    assertThat(tilskuddPeriodes.get(0).getAvtaleInnhold().getAvtale()).isNotNull();
  }


  @Test
  public void skalKunne_hente_avale_med_GODKJENT_tilskuddPeriode() {
    // Lage avtale
    tilskuddPeriode.setAvtaleInnhold(lagretAvtale.gjeldendeInnhold());
    lagretAvtale.gjeldendeInnhold().setTilskuddPeriode(Collections.singletonList(tilskuddPeriode));
    tilskuddPeriode.setBeløp(25);
    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
    tilskuddPeriode.setGodkjentTidspunkt(LocalDateTime.now());
    tilskuddPeriodeRepository.save(tilskuddPeriode);

    List<TilskuddPeriode> tilskuddPeriodes = tilskuddPeriodeRepository.findAllByStatus(TilskuddPeriodeStatus.GODKJENT);
    assertThat(tilskuddPeriodes).isNotEmpty();
    assertThat(tilskuddPeriodes.get(0).getAvtaleInnhold().getAvtale()).isNotNull();
  }

}