package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Test;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

public class AbacCachePopulatorTest {

    @Test
    public void populateCache_skal_evicte_og_hente_tilgang_for_hver_kombinasjon_av_bruker_og_veileder() {
        VeilarbabacClient veilarbabacClient = mock(VeilarbabacClient.class);;
        AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
        NavIdent veileder1 = new NavIdent("veileder1");
        NavIdent veileder2 = new NavIdent("veileder2");
        String deltaker1 = "11111111111";
        String deltaker2 = "22222222222";
        when(avtaleRepository.findAll()).thenReturn(Arrays.asList(avtale(veileder1, deltaker1), avtale(veileder2, deltaker2)));

        new AbacCachePopulator(veilarbabacClient, avtaleRepository).populateCache();
        verify(avtaleRepository).findAll();
        verify(veilarbabacClient).evict(veileder1, deltaker1, TilgangskontrollAction.read);
        verify(veilarbabacClient).evict(veileder1, deltaker2, TilgangskontrollAction.read);
        verify(veilarbabacClient).evict(veileder2, deltaker1, TilgangskontrollAction.read);
        verify(veilarbabacClient).evict(veileder2, deltaker2, TilgangskontrollAction.read);
        verify(veilarbabacClient).sjekkTilgang(veileder1, deltaker1, TilgangskontrollAction.read);
        verify(veilarbabacClient).sjekkTilgang(veileder1, deltaker2, TilgangskontrollAction.read);
        verify(veilarbabacClient).sjekkTilgang(veileder2, deltaker1, TilgangskontrollAction.read);
        verify(veilarbabacClient).sjekkTilgang(veileder2, deltaker2, TilgangskontrollAction.read);
    }

    private Avtale avtale(NavIdent veileder, String deltaker) {
        return new Avtale(new Fnr(deltaker), new BedriftNr("999999999"), veileder);
    }
}
