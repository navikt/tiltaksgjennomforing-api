package no.nav.tag.tiltaksgjennomforing.datavarehus;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncDvhAvtalePatchServiceTest {

    @Mock
    private DvhAvtalePatchService dvhAvtalePatcher;

    @InjectMocks
    private AsyncDvhAvtalePatchService asyncDvhAvtalePatchService;

    @Test
    void lagDvhPatchMeldingForAlleAvtaler_fullforer_exceptionally_nar_patch_feiler() {
        RuntimeException feil = new RuntimeException("boom");
        when(dvhAvtalePatcher.patch(eq(null), any())).thenThrow(feil);

        assertThatThrownBy(() -> asyncDvhAvtalePatchService.lagDvhPatchMeldingForAlleAvtaler().join())
            .hasCause(feil);
    }

    @Test
    void lagDvhPatchMeldingForAlleAvtaler_patcher_alle_sider() {
        var førstePageable = PageRequest.of(0, 100).withSort(Sort.Direction.ASC, "id");
        var andrePageable = PageRequest.of(1, 100).withSort(Sort.Direction.ASC, "id");
        var førsteSide = new SliceImpl<Avtale>(List.of(), førstePageable, true);
        var andreSide = new SliceImpl<Avtale>(List.of(), andrePageable, false);

        when(dvhAvtalePatcher.patch(eq(null), eq(førstePageable)))
            .thenReturn(new DvhAvtalePatcherRespons(førsteSide, 100));
        when(dvhAvtalePatcher.patch(eq(null), eq(andrePageable)))
            .thenReturn(new DvhAvtalePatcherRespons(andreSide, 50));

        asyncDvhAvtalePatchService.lagDvhPatchMeldingForAlleAvtaler().join();

        verify(dvhAvtalePatcher).patch(eq(null), eq(førstePageable));
        verify(dvhAvtalePatcher).patch(eq(null), eq(andrePageable));
        verify(dvhAvtalePatcher, times(2)).patch(eq(null), any());
    }
}
