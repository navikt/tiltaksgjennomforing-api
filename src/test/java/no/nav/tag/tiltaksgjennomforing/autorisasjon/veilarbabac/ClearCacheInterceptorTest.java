package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;;

public class ClearCacheInterceptorTest {
    
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private VeilarbabacClient abacClient = mock(VeilarbabacClient.class);
    private AxsysService axsysService = mock(AxsysService.class);
    private ClearCacheInterceptor clearCacheInterceptor = new ClearCacheInterceptor(abacClient, axsysService);

    @Test
    public void skal_evicte_cache_hvis_header_er_true() throws Exception {
        when(request.getHeader(ClearCacheInterceptor.CLEAR_CACHE_HEADER)).thenReturn("true");
        clearCacheInterceptor.preHandle(request, null, null);
        verify(abacClient).cacheEvict();
        verify(axsysService).cacheEvict();
    }
    
    @Test
    public void skal_ikke_evicte_cache_hvis_header_er_false() throws Exception {
        when(request.getHeader(ClearCacheInterceptor.CLEAR_CACHE_HEADER)).thenReturn("false");
        clearCacheInterceptor.preHandle(request, null, null);
        verifyZeroInteractions(abacClient, axsysService);
    }
    
    @Test
    public void skal_ikke_evicte_cache_hvis_header_er_tilfeldig_streng() throws Exception {
        when(request.getHeader(ClearCacheInterceptor.CLEAR_CACHE_HEADER)).thenReturn("ajsdfbgjd");
        clearCacheInterceptor.preHandle(request, null, null);
        verifyZeroInteractions(abacClient, axsysService);
    }
    
    @Test
    public void skal_ikke_evicte_cache_hvis_header_er_udefinert() throws Exception {
        clearCacheInterceptor.preHandle(request, null, null);
        verifyZeroInteractions(abacClient, axsysService);
    }
}
